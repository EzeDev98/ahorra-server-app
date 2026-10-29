package com.ahorra.features.behavior.dto;

import com.ahorra.features.behavior.enums.TrendDirection;

import java.math.BigDecimal;

public record TrendView(
        TrendDirection direction,
        BigDecimal slopePerWeek,
        BigDecimal latestRatioPercent,
        int weeksUsed,
        boolean livingAboveMeans
) {
}
