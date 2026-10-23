package com.ahorra.features.passwordreset.service;

import com.ahorra.features.otp.dto.request.VerifyOtpRequest;
import com.ahorra.features.passwordreset.dto.request.CompletePasswordResetRequest;
import com.ahorra.features.passwordreset.dto.request.PasswordResetRequest;
import com.ahorra.features.passwordreset.dto.response.PasswordResetResponse;
import com.ahorra.features.passwordreset.dto.response.VerifyOtpResponse;

public interface PasswordResetService {
    PasswordResetResponse requestOtp(PasswordResetRequest resetRequest);
    VerifyOtpResponse verifyOtp(VerifyOtpRequest request);
    PasswordResetResponse completeReset(CompletePasswordResetRequest request);
}
