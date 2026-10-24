package com.ahorra.features.income.dto.request;

import com.ahorra.features.accounting.enums.PeriodType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record IncomeRequest(
        @NotNull(message = "minAmount is required")
        @DecimalMin(value = "0.00", message = "minAmount cannot be negative")
        @Digits(integer = 13, fraction = 2, message = "minAmount allows up to 2 decimal places")
        BigDecimal minAmount,
        @NotNull(message = "maxAmount is required")
        @DecimalMin(value = "0.00", message = "maxAmount cannot be negative")
        @Digits(integer = 13, fraction = 2, message = "maxAmount allows up to 2 decimal places")
        BigDecimal maxAmount,
        @NotNull(message = "periodType is required")
        PeriodType periodType,
        @Positive(message = "periodLength must be positive")
        Integer periodLength
) {
}
