package com.ahorra.features.passwordreset.web;

import com.ahorra.common.util.RateLimiterService;
import com.ahorra.features.otp.dto.request.VerifyOtpRequest;
import com.ahorra.features.passwordreset.dto.request.CompletePasswordResetRequest;
import com.ahorra.features.passwordreset.dto.request.PasswordResetRequest;
import com.ahorra.features.passwordreset.dto.response.PasswordResetResponse;
import com.ahorra.features.passwordreset.dto.response.VerifyOtpResponse;
import com.ahorra.features.passwordreset.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

import static com.ahorra.common.constants.GeneralConstants.MAX_RESET_PASSWORD_REQUEST;
import static com.ahorra.common.constants.GeneralConstants.RESET_PASSWORD_RATE_LIMIT_DURATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("${app.title}/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/request")
    public ResponseEntity<PasswordResetResponse> requestOtp(@RequestBody PasswordResetRequest request) {
        rateLimiterService.checkRateLimit("ratelimit:password-reset-otp:email:" + String.valueOf(request.getEmail()).trim().toLowerCase(), MAX_RESET_PASSWORD_REQUEST, Duration.ofHours(RESET_PASSWORD_RATE_LIMIT_DURATION));
        PasswordResetResponse passwordResetResponse = passwordResetService.requestOtp(request);
        return ResponseEntity.ok(passwordResetResponse);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@RequestBody VerifyOtpRequest request) {
        VerifyOtpResponse verifyOtpResponse = passwordResetService.verifyOtp(request);
        return ResponseEntity.ok(verifyOtpResponse);
    }

    @PostMapping("/complete")
    public ResponseEntity<PasswordResetResponse> completeReset(@RequestBody CompletePasswordResetRequest request) {
        PasswordResetResponse passwordResetResponse = passwordResetService.completeReset(request);
        return ResponseEntity.ok(passwordResetResponse);
    }
}
