package com.ahorra.features.passwordreset.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetResponse {
    private String message;
    private int statusCode;
    private String email;
}
