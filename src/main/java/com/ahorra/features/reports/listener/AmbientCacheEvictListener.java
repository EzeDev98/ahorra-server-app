package com.ahorra.features.reports.listener;

import com.ahorra.features.day.event.DayClosedEvent;
import com.ahorra.features.reports.service.impl.AmbientAggregateCache;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AmbientCacheEvictListener {

    private final AmbientAggregateCache aggregateCache;

    public AmbientCacheEvictListener(AmbientAggregateCache aggregateCache) {
        this.aggregateCache = aggregateCache;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDayClosed(DayClosedEvent event) {
        aggregateCache.evict(event.userId());
    }
}
