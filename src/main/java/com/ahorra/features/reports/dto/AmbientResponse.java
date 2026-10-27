package com.ahorra.features.reports.dto;

import com.ahorra.features.reports.enums.ReportVerdict;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AmbientResponse(
        LocalDate date,
        BigDecimal confirmedSpend,
        BigDecimal pendingSpend,
        int pendingCount,
        BigDecimal provisionalSpend,
        boolean  hasUnconfirmed,
        BigDecimal usualDailySpend,
        int daysObserved,
        boolean comparisonAvailable,
        ReportVerdict verdict,
        BigDecimal periodVariance,
        BigDecimal periodBaseline
) {
}
