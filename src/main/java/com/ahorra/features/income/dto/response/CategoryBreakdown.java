package com.ahorra.features.income.dto.response;

import java.math.BigDecimal;

public record CategoryBreakdown(
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        BigDecimal shareOfSpendPercent,
        BigDecimal attributedExcess
) {
}
