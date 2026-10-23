package com.ahorra.features.passwordreset.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompletePasswordResetRequest {
    private String resetToken;
    private String newPassword;
    private String confirmPassword;
}
