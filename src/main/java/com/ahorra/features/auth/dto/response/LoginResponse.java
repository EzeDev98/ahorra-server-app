package com.ahorra.features.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private Long userId;
    private String firstName;
    private String username;
    private String message;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private Long refreshExpiresIn;
}
