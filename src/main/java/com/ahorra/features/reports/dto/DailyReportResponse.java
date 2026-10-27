package com.ahorra.features.reports.dto;

import com.ahorra.features.accounting.enums.PeriodType;
import com.ahorra.features.reports.enums.NarrativeSource;
import com.ahorra.features.reports.enums.ReportKind;
import com.ahorra.features.reports.enums.ReportVerdict;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyReportResponse(
        LocalDate date,
        ReportKind kind,
        String summary,
        PeriodType periodType,
        LocalDate periodStart,
        LocalDate periodEnd,
        int entryCount,
        BigDecimal daySpend,
        BigDecimal baseline,
        BigDecimal incomeMax,
        BigDecimal periodSpend,
        BigDecimal variance,
        BigDecimal rollingAvgDailySpend,
        Integer daysObserved,
        ReportVerdict verdict,
        String insight,
        NarrativeSource narrativeSource,
        List<CategoryLine> dayCategories,
        List<CategoryLine> periodCategories
) {
}
