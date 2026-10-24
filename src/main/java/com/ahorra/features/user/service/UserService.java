package com.ahorra.features.user.service;

import com.ahorra.features.auth.dto.request.RegistrationRequest;
import com.ahorra.features.auth.dto.response.UserRegistrationResponse;

public interface UserService {
    UserRegistrationResponse registerUser(RegistrationRequest request);
}
