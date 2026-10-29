package com.ahorra.features.behavior.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CategoryDaySpend(
        LocalDate day,
        Long categoryId,
        BigDecimal amount
) {
}
