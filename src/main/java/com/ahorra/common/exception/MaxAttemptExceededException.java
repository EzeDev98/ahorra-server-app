package com.ahorra.common.exception;

public class MaxAttemptExceededException extends RuntimeException {
    public MaxAttemptExceededException(String message) {
        super(message);
    }
}
