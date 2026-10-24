package com.ahorra.features.auth.service;

import com.ahorra.features.auth.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(String username, String password);
    LoginResponse refreshAccessToken(String refreshToken);
}
