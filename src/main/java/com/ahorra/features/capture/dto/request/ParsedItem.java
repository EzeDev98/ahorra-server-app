package com.ahorra.features.capture.dto.request;

import java.math.BigDecimal;

public record ParsedItem(
        String description,
        BigDecimal amount
) {
}
