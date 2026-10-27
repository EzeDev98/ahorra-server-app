package com.ahorra.features.reports.dto;

import com.ahorra.features.reports.enums.ReportKind;
import com.ahorra.features.reports.enums.ReportVerdict;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyReportSummary(
        LocalDate date,
        ReportKind kind,
        Integer entryCount,
        BigDecimal daySpend,
        ReportVerdict verdict,
        String summary
) {
}
