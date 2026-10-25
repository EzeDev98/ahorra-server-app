package com.ahorra.features.capture.dto.request;

import com.ahorra.features.capture.enums.EntrySource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CaptureRequest(
        @NotBlank(message = "Entry text is required")
        @Size(max = 2000, message = "Entry is too long (max 2000 characters)")
        String text,
        EntrySource source,
        @Size(max = 100, message = "clientRequestId is too long")
        String clientRequestId
) {
}
