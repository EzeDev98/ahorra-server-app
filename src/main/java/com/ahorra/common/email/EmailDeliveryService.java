package com.ahorra.common.email;

public interface EmailDeliveryService {
    void sendVerificationOtpEmail(String email, String otp, String appName);
    void sendPasswordResetOtpEmail(String email, String otp, String appName);
}
