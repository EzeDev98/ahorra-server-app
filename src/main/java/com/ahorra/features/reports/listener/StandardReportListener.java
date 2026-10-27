package com.ahorra.features.reports.listener;

import com.ahorra.features.day.event.DayClosedEvent;
import com.ahorra.features.reports.service.DailyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StandardReportListener {

    private final DailyReportService dailyReportService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    private void onDayClosed(DayClosedEvent event) {
        try {
            dailyReportService.generateIfMissing(event.userId(), event.day());
        } catch (RuntimeException ex) {
            log.error("Report generation failed for user {} on {}", event.userId(), event.day(), ex);
        }
    }
}
