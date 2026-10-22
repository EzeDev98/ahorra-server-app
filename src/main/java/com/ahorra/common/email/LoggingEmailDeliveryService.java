package com.ahorra.common.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.email", name = "provider", havingValue = "logging", matchIfMissing = true)
public class LoggingEmailDeliveryService implements EmailDeliveryService {

    @Value("${app.email.log-to-console:false}")
    private boolean logToConsole;

    @Override
    public void sendVerificationOtpEmail(String email, String otp, String appName) {
        if (logToConsole) {
            log.info("[DEV ONLY] Email verification code for {} (app={}): {}", email, appName, otp);
        } else {
            log.info("Email verification code generated for {} (app={}, delivery provider not configured)", email, appName);
        }
    }

    @Override
    public void sendPasswordResetOtpEmail(String email, String otp, String appName) {
        if (logToConsole) {
            log.info("[DEV ONLY] Password reset code for {} (app={}): {}", email, appName, otp);
        } else {
            log.info("Password reset code generated for {} (app={}, delivery provider not configured)", email, appName);
        }
    }
}
