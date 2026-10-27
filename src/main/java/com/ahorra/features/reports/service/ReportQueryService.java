package com.ahorra.features.reports.service;

import com.ahorra.features.reports.dto.DailyReportResponse;
import com.ahorra.features.reports.dto.DailyReportSummary;

import java.time.LocalDate;
import java.util.List;

public interface ReportQueryService {
    DailyReportResponse forDay(Long userId, LocalDate day);
    DailyReportResponse latest(Long userId);
    List<DailyReportSummary> history(Long userId, LocalDate from, LocalDate to);
}
