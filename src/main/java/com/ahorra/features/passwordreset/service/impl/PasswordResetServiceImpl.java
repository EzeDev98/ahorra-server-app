package com.ahorra.features.passwordreset.service.impl;

import com.ahorra.common.email.EmailDeliveryService;
import com.ahorra.common.exception.BadRequestException;
import com.ahorra.features.otp.dto.request.VerifyOtpRequest;
import com.ahorra.features.passwordreset.config.PasswordResetProperties;
import com.ahorra.features.passwordreset.dto.request.CompletePasswordResetRequest;
import com.ahorra.features.passwordreset.dto.request.PasswordResetRequest;
import com.ahorra.features.passwordreset.dto.response.PasswordResetResponse;
import com.ahorra.features.passwordreset.dto.response.VerifyOtpResponse;
import com.ahorra.features.passwordreset.model.PasswordReset;
import com.ahorra.features.passwordreset.repository.PasswordResetRepository;
import com.ahorra.features.passwordreset.service.PasswordResetService;
import com.ahorra.features.session.service.SessionManagementSession;
import com.ahorra.features.user.enums.UserStatus;
import com.ahorra.features.user.model.User;
import com.ahorra.features.user.repository.UserRepository;
import com.ahorra.features.user.utility.RegistrationValidator;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

import static com.ahorra.common.constants.GeneralConstants.APP_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String REQUEST_MESSAGE = "If this email address is registered, a verification code has been sent.";
    private static final String INVALID_CODE = "The code is invalid or has expired. Request a new one.";
    private static final String INVALID_TOKEN = "This reset session has expired. Start again.";

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailDeliveryService emailDeliveryService;
    private final PasswordResetProperties properties;
    private final RegistrationValidator registrationValidator;
    private final SessionManagementSession sessionManagementService;

    @Override
    public PasswordResetResponse requestOtp(PasswordResetRequest request) {
        String email = request.getEmail();

        if (email != null && !email.isBlank()) {
            userRepository.findByEmailIgnoreCase(email)
                    .filter(user -> user.getUserStatus() == UserStatus.ACTIVE)
                    .ifPresent(this::issueOtp);
        }

        return new PasswordResetResponse(REQUEST_MESSAGE, HttpServletResponse.SC_OK, email);
    }

    @Override
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            throw new BadRequestException(INVALID_CODE);
        }

        PasswordReset reset = userRepository.findByEmailIgnoreCase(email)
                .flatMap(user -> passwordResetRepository.findAllByUserIdAndUsedAtIsNull(user.getId()).stream().max(Comparator.comparing(PasswordReset::getCreatedAt)))
                .orElseThrow(() -> new BadRequestException(INVALID_CODE));

        LocalDateTime now = LocalDateTime.now();
        boolean unusable = reset.getResetTokenHash() != null || reset.getOtpExpiresAt().isBefore(now) || reset.getAttempts() >= properties.getOtp().getMaxAttempts();
        if (unusable) {
            throw new BadRequestException(INVALID_CODE);
        }

        reset.setAttempts(reset.getAttempts() + 1);
        if (!passwordEncoder.matches(otp, reset.getOtpHash())) {
            passwordResetRepository.save(reset);
            throw new BadRequestException(INVALID_CODE);
        }

        String token = newResetToken();
        reset.setResetTokenHash(sha256(token));
        reset.setTokenExpiresAt(now.plusMinutes(properties.getToken().getExpirationMinutes()));
        passwordResetRepository.save(reset);

        return new VerifyOtpResponse(token);
    }

    @Override
    @Transactional
    public PasswordResetResponse completeReset(CompletePasswordResetRequest request) {
        registrationValidator.validateNewPassword(request.getNewPassword(), request.getConfirmPassword());

        String token = request.getResetToken();
        if (token == null || token.isBlank()) {
            throw new BadRequestException(INVALID_TOKEN);
        }

        LocalDateTime now = LocalDateTime.now();
        PasswordReset reset = passwordResetRepository.findByResetTokenHash(sha256(token))
                .filter(found -> found.getUsedAt() == null && found.getTokenExpiresAt() != null && found.getTokenExpiresAt().isAfter(now))
                .orElseThrow(() -> new BadRequestException(INVALID_TOKEN));

        User user = userRepository.findById(reset.getUserId())
                .orElseThrow(() -> new BadRequestException(INVALID_TOKEN));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        reset.setUsedAt(now);
        passwordResetRepository.save(reset);

        sessionManagementService.invalidateUserSessions(user.getId());

        log.info("Password reset completed for user {}", user.getUsername());
        return new PasswordResetResponse("Password reset successful", HttpServletResponse.SC_OK, user.getEmail());
    }

    private void issueOtp(User user) {
        LocalDateTime now = LocalDateTime.now();
        List<PasswordReset> open = passwordResetRepository.findAllByUserIdAndUsedAtIsNull(user.getId());

        int cooldown = properties.getOtp().getResendCooldownSeconds();
        boolean requestedJustNow = open.stream().anyMatch(earlier -> earlier.getCreatedAt().plusSeconds(cooldown).isAfter(now));
        if (requestedJustNow) {
            log.info("Password reset code for user {} was requested again inside the cooldown; ignored", user.getUsername());
            return;
        }

        open.forEach(earlier -> earlier.setUsedAt(now));
        passwordResetRepository.saveAll(open);

        String code = newCode(properties.getOtp().getLength());
        PasswordReset reset = new PasswordReset();
        reset.setUserId(user.getId());
        reset.setOtpHash(passwordEncoder.encode(code));
        reset.setOtpExpiresAt(now.plusMinutes(properties.getOtp().getExpirationMinutes()));
        reset.setCreatedAt(now);
        passwordResetRepository.save(reset);

        emailDeliveryService.sendPasswordResetOtpEmail(user.getEmail(), code, APP_NAME);
    }

    private static String newCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }

    private static String newResetToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
