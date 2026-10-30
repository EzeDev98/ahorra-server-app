package com.ahorra.features.push.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PushSubscriptionRequest(

        @NotBlank(message = "endpoint is required")
        @Size(max = 512, message = "endpoint is too long (max 512 characters)")
        @Pattern(regexp = "^https://.+", message = "endpoint must be an https address")
        String endpoint,
        @NotNull(message = "keys are required")
        @Valid
        Keys keys
) {
    public record Keys(
            @NotBlank(message = "p256dh is required")
            @Size(max = 255, message = "p256dh is too long")
            String p256dh,
            @NotBlank(message = "auth is required")
            @Size(max = 255, message = "auth is too long")
            String auth
    ) {
    }
}
