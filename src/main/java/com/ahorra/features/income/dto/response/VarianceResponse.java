package com.ahorra.features.income.dto.response;

import com.ahorra.features.accounting.enums.PeriodType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VarianceResponse(
        LocalDate asOf,
        PeriodType periodType,
        LocalDate periodStart,
        LocalDate periodEnd,
        int daysElapsed,
        int daysInPeriod,
        BigDecimal incomeMin,
        BigDecimal incomeMax,
        BigDecimal baseline,
        BigDecimal spend,
        BigDecimal variance,
        List<CategoryBreakdown> categories
) {
}
