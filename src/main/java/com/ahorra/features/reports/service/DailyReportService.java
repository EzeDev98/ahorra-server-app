package com.ahorra.features.reports.service;

import java.time.LocalDate;

public interface DailyReportService {
    void generateIfMissing(Long userId, LocalDate day);
    void generateMissing(Long userId);
}
