package com.ahorra.features.behavior.dto;

import com.ahorra.features.behavior.enums.DayType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BaselineView(
        String category,
        int windowDays,
        DayType dayType,
        BigDecimal mean,
        BigDecimal stdDev,
        int sampleDays,
        BigDecimal confidence,
        LocalDate computedThrough
) {
}
