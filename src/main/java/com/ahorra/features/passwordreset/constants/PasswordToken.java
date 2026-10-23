package com.ahorra.features.passwordreset.constants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordToken {
    private int expirationMinutes = 10;
}
