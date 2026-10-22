package com.ahorra.features.emailVerification.service;

import com.ahorra.features.emailVerification.dto.ResendEmailVerificationRequest;
import com.ahorra.features.otp.dto.request.VerifyOtpRequest;
import com.ahorra.features.otp.dto.response.ActivationResponse;
import com.ahorra.features.user.model.User;

public interface EmailVerificationService {
    void sendCode(User user);
    ActivationResponse verifyEmail(VerifyOtpRequest request);
    ActivationResponse resendVerification(ResendEmailVerificationRequest request);
}
