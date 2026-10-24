package com.ahorra.common.exception;

import lombok.Getter;

@Getter
public class IncorrectPinException extends RuntimeException {
    private final int attemptsRemaining;

    public IncorrectPinException(String message, int attemptsRemaining) {
        super(message);
        this.attemptsRemaining = attemptsRemaining;
    }
}
