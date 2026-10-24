package com.ahorra.features.pin.service.impl;

import com.ahorra.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.ahorra.common.constants.GeneralConstants.PIN_LENGTH;

@Component
public class PinPolicy {

    private static final Set<String> COMMON = Set.of("123456", "654321", "123123", "112233", "121212", "696969", "159753", "123321");

    public void assertAcceptable(String pin, String confirmPin) {
        assertFormat(pin);
        if (!pin.equals(confirmPin)) {
            throw new BadRequestException("PINs do not match");
        }
        if (isWeak(pin)) {
            throw new BadRequestException("PIN is too easy to guess. Choose a different PIN.");
        }
    }

    public void assertFormat(String pin) {
        if (pin == null || !pin.matches("\\d{" + PIN_LENGTH + "}")) {
            throw new BadRequestException("PIN must be exactly " + PIN_LENGTH + " digits");
        }
    }

    private boolean isWeak(String pin) {
        return COMMON.contains(pin) || allSame(pin) || isRun(pin) || repeatsBlock(pin, 2) || repeatsBlock(pin, 3);
    }

    private boolean allSame(String pin) {
        return pin.chars().allMatch(c -> c == pin.charAt(0));
    }

    private boolean isRun(String pin) {
        boolean ascending = true;
        boolean descending = true;
        for (int i = 0; i < pin.length() - 1; i++) {
            int step = pin.charAt(i + 1) - pin.charAt(i);
            if (step != 1) ascending = false;
            if (step != -1) descending = false;
        }
        return ascending || descending;
    }

    private boolean repeatsBlock(String pin, int size) {
        if (pin.length() % size != 0) return false;
        String block = pin.substring(0, size);
        return block.repeat(pin.length() / size).equals(pin);
    }
}
