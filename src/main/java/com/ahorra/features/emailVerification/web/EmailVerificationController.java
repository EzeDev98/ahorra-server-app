package com.ahorra.features.emailVerification.web;

import com.ahorra.features.emailVerification.dto.ResendEmailVerificationRequest;
import com.ahorra.features.emailVerification.service.EmailVerificationService;
import com.ahorra.features.otp.dto.request.VerifyOtpRequest;
import com.ahorra.features.otp.dto.response.ActivationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${app.title}/email")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/verify")
    public ResponseEntity<ActivationResponse> verify(@RequestBody VerifyOtpRequest request) {
        ActivationResponse response = emailVerificationService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ActivationResponse> resendVerification(@RequestBody ResendEmailVerificationRequest request) {
        ActivationResponse response = emailVerificationService.resendVerification(request);
        return ResponseEntity.ok(response);
    }
}
