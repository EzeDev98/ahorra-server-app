package com.ahorra.common.exception;

public class RequiredFieldsException extends RuntimeException {
    public RequiredFieldsException(String message) {
        super(message);
    }
}
