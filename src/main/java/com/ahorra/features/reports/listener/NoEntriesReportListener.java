package com.ahorra.features.reports.listener;

import com.ahorra.features.accounting.repository.LedgerEntryRepository;
import com.ahorra.features.day.event.DayClosedEvent;
import com.ahorra.features.reports.enums.ReportKind;
import com.ahorra.features.reports.model.DailyReport;
import com.ahorra.features.reports.repository.DailyReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoEntriesReportListener {

    static final String NO_ENTRIES_SUMMARY = "No entries logged today";
    private final LedgerEntryRepository ledgerEntryRepository;
    private final DailyReportRepository dailyReportRepository;

    @EventListener
    public void onDayClosed(DayClosedEvent event) {

        if (event.retroactive()) {
            return;
        }

        if (ledgerEntryRepository.countByUserIdAndEntryDate(event.userId(), event.day()) > 0) {
            return;
        }

        if (dailyReportRepository.existsByUserIdAndReportDate(event.userId(), event.day())) {
            return;
        }

        DailyReport report = new DailyReport();
        report.setUserId(event.userId());
        report.setReportDate(event.day());
        report.setKind(ReportKind.NO_ENTRIES);
        report.setSummaryText(NO_ENTRIES_SUMMARY);
        dailyReportRepository.save(report);
        log.info("Stored no entries report for user {} on {}", event.userId(), event.day());
    }
}
