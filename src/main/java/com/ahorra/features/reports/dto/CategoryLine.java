package com.ahorra.features.reports.dto;

import java.math.BigDecimal;

public record CategoryLine(
        String name,
        BigDecimal amount,
        BigDecimal sharePercent,
        BigDecimal attributedExcess
) {
}
