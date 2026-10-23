package com.ahorra.features.emailVerification.service.impl;

import com.ahorra.common.email.EmailDeliveryService;
import com.ahorra.common.exception.BadRequestException;
import com.ahorra.common.exception.MaxAttemptExceededException;
import com.ahorra.features.emailVerification.dto.ResendEmailVerificationRequest;
import com.ahorra.features.emailVerification.model.EmailVerification;
import com.ahorra.features.emailVerification.repository.EmailVerificationRepository;
import com.ahorra.features.emailVerification.service.EmailVerificationService;
import com.ahorra.features.otp.dto.request.VerifyOtpRequest;
import com.ahorra.features.otp.dto.response.ActivationResponse;
import com.ahorra.features.user.enums.UserStatus;
import com.ahorra.features.user.model.User;
import com.ahorra.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static com.ahorra.common.constants.GeneralConstants.APP_NAME;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private static final String INVALID_CODE = "The code is invalid or has expired. Request a new one.";
    private static final String RESEND_MESSAGE = "If this email address is registered and not yet verified, a new code has been sent.";

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailDeliveryService emailDeliveryService;

    @Override
    public void sendCode(User user) {
        LocalDateTime now = LocalDateTime.now();
        List<EmailVerification> emails = emailVerificationRepository.findAllByUserIdAndUsedAtIsNull(user.getId());

        long waitSeconds = emails.stream()
                .mapToLong(earlier -> {
                    long remaining = Duration.between(now, earlier.getCreatedAt().plusSeconds(RESEND_COOLDOWN_SECONDS)).toMillis();
                    return remaining > 0 ? (remaining + 999) / 1000 : 0;
                })
                .max()
                .orElse(0);
        if (waitSeconds > 0) {
            throw new MaxAttemptExceededException("Please wait " + waitSeconds + " seconds before requesting another code");
        }

        emails.forEach(earlier -> earlier.setUsedAt(now));
        emailVerificationRepository.saveAll(emails);

        String code = newCode();
        EmailVerification verification = new EmailVerification();
        verification.setUserId(user.getId());
        verification.setOtpHash(passwordEncoder.encode(code));
        verification.setOtpExpiresAt(now.plusMinutes(EXPIRATION_MINUTES));
        verification.setCreatedAt(now);
        emailVerificationRepository.save(verification);

        emailDeliveryService.sendVerificationOtpEmail(user.getEmail(), code, APP_NAME);
    }

    @Override
    public ActivationResponse resendVerification(ResendEmailVerificationRequest request) {
        String email = request.getEmail();

        if (email != null && !email.isBlank()) {
            userRepository.findByEmailIgnoreCase(email)
                    .filter(user -> !user.isEmailVerified())
                    .ifPresent(this::sendCode);
        }

        return new ActivationResponse(RESEND_MESSAGE);
    }

    @Override
    public ActivationResponse verifyEmail(VerifyOtpRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            throw new BadRequestException(INVALID_CODE);
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException(INVALID_CODE));

        EmailVerification verification = emailVerificationRepository.findAllByUserIdAndUsedAtIsNull(user.getId()).stream()
                .max(Comparator.comparing(EmailVerification::getCreatedAt))
                .orElseThrow(() -> new BadRequestException(INVALID_CODE));

        LocalDateTime now = LocalDateTime.now();
        if (verification.getOtpExpiresAt().isBefore(now) || verification.getAttempts() >= MAX_ATTEMPTS) {
            throw new BadRequestException(INVALID_CODE);
        }

        verification.setAttempts(verification.getAttempts() + 1);
        if (!passwordEncoder.matches(otp, verification.getOtpHash())) {
            emailVerificationRepository.save(verification);
            throw new BadRequestException(INVALID_CODE);
        }

        user.setEmailVerified(true);
        user.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        verification.setUsedAt(now);
        emailVerificationRepository.save(verification);

        log.info("Email verified for user {}", user.getUsername());
        return new ActivationResponse("Email address verified. You can now sign in.");
    }

    private static String newCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }
}
