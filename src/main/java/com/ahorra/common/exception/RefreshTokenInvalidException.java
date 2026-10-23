package com.ahorra.common.exception;

public class RefreshTokenInvalidException extends RuntimeException {
    public RefreshTokenInvalidException(String message) {
        super(message);
    }

    public RefreshTokenInvalidException(String message, Throwable ex) {
        super(message, ex);
    }
}
