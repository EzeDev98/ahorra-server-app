package com.ahorra.features.capture.dto.request;

import com.ahorra.features.capture.enums.FlowDirection;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ConfirmRequest(
        FlowDirection flow,
        LocalDate entryDate,
        @Size(max = 255, message = "Vendor is too long (max 255 characters)")
        String vendor,
        @Positive(message = "categoryId must be positive")
        Long categoryId,
        @Positive(message = "accountId must be  positive")
        Long accountId,
        @Size(max = 50, message = "Too many items in one entry (max 50)")
        List<ParsedItem> items
) {
    public boolean hasEdits() {
        return flow != null || entryDate != null || vendor != null || categoryId != null || accountId != null || items != null;
    }
}
