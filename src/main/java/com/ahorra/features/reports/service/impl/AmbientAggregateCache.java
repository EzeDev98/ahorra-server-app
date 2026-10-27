package com.ahorra.features.reports.service.impl;

import com.ahorra.common.cache.RedisJsonCache;
import com.ahorra.features.day.dto.request.DaySpend;
import com.ahorra.features.day.service.impl.DailySpendService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class AmbientAggregateCache {

    private static final int WINDOW_DAYS = 30;
    private static final Duration TTL = Duration.ofHours(25);

    public record UsualSpend(BigDecimal usualDailySpend, int daysObserved, boolean comparisonAvailable) {
    }

    private final DailySpendService dailySpendService;
    private final RedisJsonCache cache;
    private final ZoneId zone;

    public AmbientAggregateCache(DailySpendService dailySpendService, RedisJsonCache cache, @Value("${app.timezone:Africa/Lagos}") String timezone) {
        this.dailySpendService = dailySpendService;
        this.cache = cache;
        this.zone = ZoneId.of(timezone);
    }

    public UsualSpend usualSpend(Long userId, LocalDate today) {
        String key = key(userId, today);
        Optional<UsualSpend> cached = cache.get(key, UsualSpend.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        UsualSpend fresh = compute(userId, today);
        cache.put(key, fresh, TTL);
        return fresh;
    }

    public void evict(Long userId) {
        LocalDate today = LocalDate.now(zone);
        cache.evict(key(userId, today));
        cache.evict(key(userId, today.plusDays(1)));
    }

    private UsualSpend compute(Long userId, LocalDate today) {
        List<DaySpend> history = dailySpendService.closedDaySpend(userId, today.minusDays(WINDOW_DAYS), today.minusDays(1));
        boolean available = history.size() >= WINDOW_DAYS;
        BigDecimal usual = null;
        if (available) {
            BigDecimal total = history.stream()
                    .map(DaySpend::spend)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            usual = total.divide(BigDecimal.valueOf(history.size()), 2, RoundingMode.HALF_UP);
        }
        return new UsualSpend(usual, history.size(), available);
    }

    private String key(Long userId, LocalDate day) {
        return "ahorra:ambient:usual:v1:" + userId + ":" + day;
    }
}
