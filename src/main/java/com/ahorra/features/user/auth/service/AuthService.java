package com.ahorra.features.user.auth.service;

import com.ahorra.features.user.auth.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(String username, String password);
    LoginResponse refreshAccessToken(String refreshToken);
}
