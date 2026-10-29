package com.ahorra.features.behavior.listener;

import com.ahorra.features.behavior.service.BehaviorAnalysisService;
import com.ahorra.features.day.event.DayClosedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class BehaviorRefreshListener {

    private final BehaviorAnalysisService analysisService;

    public BehaviorRefreshListener(BehaviorAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDayClosed(DayClosedEvent event) {
        try {
            analysisService.refresh(event.userId(), event.day());
        } catch (Exception e) {
            log.warn("Behavior refresh failed for user {} through {}", event.userId(), event.day(), e);
        }
    }
}

