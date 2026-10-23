package com.ahorra.features.session.service;

import com.ahorra.features.session.dto.LogoutResponse;

public interface LogoutService {
    LogoutResponse logout(String token);
}
