package com.ahorra.features.reports.service.impl;

import com.ahorra.features.reports.dto.CategoryLine;
import com.ahorra.features.reports.dto.DailyReportResponse;
import com.ahorra.features.reports.dto.DailyReportSummary;
import com.ahorra.features.reports.enums.CategoryScope;
import com.ahorra.features.reports.model.DailyReport;
import com.ahorra.features.reports.model.DailyReportCategory;
import com.ahorra.features.reports.repository.DailyReportRepository;
import com.ahorra.features.reports.service.ReportQueryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class ReportQueryServiceImpl implements ReportQueryService {

    private static final int DEFAULT_SPAN_DAYS = 30;
    private static final int MAX_SPAN_DAYS = 92;

    private final DailyReportRepository dailyReportRepository;
    private final ZoneId zone;

    public ReportQueryServiceImpl(DailyReportRepository dailyReportRepository, @Value("${app.timezone:Africa/Lagos}") String timezone) {
        this.dailyReportRepository = dailyReportRepository;
        this.zone = ZoneId.of(timezone);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyReportResponse forDay(Long userId, LocalDate day) {
        DailyReport report = dailyReportRepository.findByUserIdAndReportDate(userId, day)
                .orElseThrow(() -> new EntityNotFoundException("No report for " + day));
        return toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyReportResponse latest(Long userId) {
        DailyReport report = dailyReportRepository.findFirstByUserIdOrderByReportDateDesc(userId)
                .orElseThrow(() -> new EntityNotFoundException("No reports yet"));
        return toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyReportSummary> history(Long userId, LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now(zone);
        LocalDate start = from != null ? from : end.minusDays(DEFAULT_SPAN_DAYS - 1L);
        if (start.isAfter(end)) {
            return List.of();
        }
        LocalDate earliest = end.minusDays(MAX_SPAN_DAYS - 1L);
        if (start.isBefore(earliest)) {
            start = earliest;
        }
        return dailyReportRepository
                .findByUserIdAndReportDateBetweenOrderByReportDateDesc(userId, start, end)
                .stream()
                .map(report -> new DailyReportSummary(
                        report.getReportDate(), report.getKind(), report.getEntryCount(),
                        report.getDaySpend(), report.getVerdict(), report.getSummaryText()))
                .toList();
    }

    private DailyReportResponse toResponse(DailyReport report) {
        return new DailyReportResponse(
                report.getReportDate(), report.getKind(), report.getSummaryText(),
                report.getPeriodType(), report.getPeriodStart(), report.getPeriodEnd(),
                report.getEntryCount(), report.getDaySpend(), report.getBaseline(), report.getIncomeMax(),
                report.getPeriodSpend(), report.getVariance(), report.getRollingAvgDailySpend(),
                report.getDaysObserved(), report.getVerdict(), report.getInsightText(),
                report.getNarrativeSource(),
                lines(report, CategoryScope.DAY),
                lines(report, CategoryScope.PERIOD));
    }

    private List<CategoryLine> lines(DailyReport report, CategoryScope scope) {
        return report.getCategories().stream()
                .filter(category -> category.getScope() == scope)
                .map(this::toLine)
                .toList();
    }

    private CategoryLine toLine(DailyReportCategory category) {
        return new CategoryLine(category.getCategoryName(), category.getAmount(), category.getSharePercent(), category.getAttributedExcess());
    }
}
