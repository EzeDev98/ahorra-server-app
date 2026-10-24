package com.ahorra.features.pin.service.impl;

import com.ahorra.common.exception.BadRequestException;
import com.ahorra.common.exception.IncorrectPinException;
import com.ahorra.common.exception.PinLockedException;
import com.ahorra.features.pin.dto.request.ChangePinRequest;
import com.ahorra.features.pin.dto.request.PinRequest;
import com.ahorra.features.pin.dto.request.VerifyPinRequest;
import com.ahorra.features.pin.dto.response.PinStatusResponse;
import com.ahorra.features.pin.model.UserPin;
import com.ahorra.features.pin.repository.UserPinRepository;
import com.ahorra.features.pin.service.PinService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.ahorra.common.constants.GeneralConstants.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class PinServiceImpl implements PinService {

    private final UserPinRepository userPinRepository;
    private final PinPolicy pinPolicy;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public PinStatusResponse status() {
        return toStatus(userPinRepository.findById(currentUserId()).orElse(null));
    }

    @Override
    @Transactional
    public PinStatusResponse setPin(PinRequest request) {
        Long userId = currentUserId();
        pinPolicy.assertAcceptable(request.getPin(), request.getConfirmPin());

        if (userPinRepository.findForUpdate(userId).isPresent()) {
            throw new BadRequestException("A PIN is already set. Change it instead.");
        }

        UserPin pin = new UserPin();
        pin.setUserId(userId);
        store(pin, request.getPin());
        return toStatus(userPinRepository.save(pin));
    }

    @Override
    @Transactional(noRollbackFor = {IncorrectPinException.class, PinLockedException.class})
    public PinStatusResponse changePin(ChangePinRequest request) {
        pinPolicy.assertAcceptable(request.getPin(), request.getConfirmPin());
        pinPolicy.assertFormat(request.getOldPin());

        UserPin pin = lockedRow();
        checkPin(pin, request.getOldPin());

        if (passwordEncoder.matches(request.getPin(), pin.getPinHash())) {
            throw new BadRequestException("The new PIN must be different from the current one.");
        }

        store(pin, request.getPin());
        return toStatus(userPinRepository.save(pin));
    }

    @Override
    @Transactional(noRollbackFor = {IncorrectPinException.class, PinLockedException.class})
    public void verifyPin(VerifyPinRequest request) {
        pinPolicy.assertFormat(request.getPin());
        checkPin(lockedRow(), request.getPin());
    }

    @Override
    @Transactional(noRollbackFor = {IncorrectPinException.class, PinLockedException.class})
    public PinStatusResponse removePin(VerifyPinRequest request) {
        pinPolicy.assertFormat(request.getPin());
        UserPin pin = lockedRow();
        checkPin(pin, request.getPin());

        userPinRepository.delete(pin);
        return toStatus(null);
    }

    @Override
    @Transactional
    public PinStatusResponse resetPin(PinRequest request) {
        Long userId = currentUserId();
        pinPolicy.assertAcceptable(request.getPin(), request.getConfirmPin());

        UserPin pin = userPinRepository.findForUpdate(userId)
                .orElseThrow(() -> new BadRequestException("No PIN is set."));

        LocalDateTime verifiedAt = pin.getLoginVerifiedAt();
        if (verifiedAt == null || verifiedAt.isBefore(LocalDateTime.now().minusMinutes(PIN_RESET_WINDOW_MINUTES))) {
            throw new AccessDeniedException("Sign in again to replace your PIN.");
        }

        store(pin, request.getPin());
        log.info("PIN replaced after sign-in for user id {}", userId);
        return toStatus(userPinRepository.save(pin));
    }

    @Override
    @Transactional
    public void markFreshLogin(Long userId) {
        userPinRepository.findForUpdate(userId).ifPresent(pin -> {
            pin.setLoginVerifiedAt(LocalDateTime.now());
            userPinRepository.save(pin);
        });
    }

    private Long currentUserId() {
        return currentUserService.getCurrentUser().getId();
    }

    private UserPin lockedRow() {
        return userPinRepository.findForUpdate(currentUserId())
                .orElseThrow(() -> new BadRequestException("No PIN is set."));
    }

    private void checkPin(UserPin pin, String rawPin) {
        long wait = remainingLockSeconds(pin);
        if (wait > 0) {
            throw lockedException(wait);
        }

        if (passwordEncoder.matches(rawPin, pin.getPinHash())) {
            pin.setFailedAttempts(0);
            pin.setLockedUntil(null);
            userPinRepository.save(pin);
            return;
        }

        int attempts = pin.getFailedAttempts() + 1;
        if (attempts >= MAX_PIN_ATTEMPTS) {
            pin.setFailedAttempts(0);
            pin.setLockedUntil(LocalDateTime.now().plusMinutes(PIN_LOCK_MINUTES));
            userPinRepository.save(pin);
            log.warn("PIN locked for user id {} after {} wrong attempts", pin.getUserId(), MAX_PIN_ATTEMPTS);
            throw lockedException(remainingLockSeconds(pin));
        }

        pin.setFailedAttempts(attempts);
        userPinRepository.save(pin);
        throw new IncorrectPinException("Incorrect PIN", MAX_PIN_ATTEMPTS - attempts);
    }

    private void store(UserPin pin, String rawPin) {
        pin.setPinHash(passwordEncoder.encode(rawPin));
        pin.setFailedAttempts(0);
        pin.setLockedUntil(null);
        pin.setChangedAt(LocalDateTime.now());
    }

    private PinLockedException lockedException(long seconds) {
        return new PinLockedException("Too many wrong attempts. Try again later.", seconds);
    }

    private long remainingLockSeconds(UserPin pin) {
        if (pin.getLockedUntil() == null) return 0;
        long millis = Duration.between(LocalDateTime.now(), pin.getLockedUntil()).toMillis();
        return millis <= 0 ? 0 : (millis + 999) / 1000;
    }

    private PinStatusResponse toStatus(UserPin pin) {
        if (pin == null) return new PinStatusResponse(false, false, 0);
        long wait = remainingLockSeconds(pin);
        return new PinStatusResponse(true, wait > 0, wait);
    }
}
