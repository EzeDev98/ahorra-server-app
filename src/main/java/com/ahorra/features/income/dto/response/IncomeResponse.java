package com.ahorra.features.income.dto.response;

import com.ahorra.features.accounting.enums.PeriodType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeResponse(
        Long id,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        PeriodType periodType,
        Integer periodLength,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
