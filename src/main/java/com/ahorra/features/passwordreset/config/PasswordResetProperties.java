package com.ahorra.features.passwordreset.config;

import com.ahorra.features.passwordreset.constants.PasswordOtp;
import com.ahorra.features.passwordreset.constants.PasswordToken;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "password-reset")
public class PasswordResetProperties {
    private PasswordOtp otp = new PasswordOtp();
    private PasswordToken token = new PasswordToken();
}
