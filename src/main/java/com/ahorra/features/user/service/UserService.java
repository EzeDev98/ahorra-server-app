package com.ahorra.features.user.service;

import com.ahorra.features.user.auth.dto.request.RegistrationRequest;
import com.ahorra.features.user.auth.dto.response.UserRegistrationResponse;

public interface UserService {
    UserRegistrationResponse registerUser(RegistrationRequest request);
}
