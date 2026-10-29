package com.ahorra.features.behavior.dto;

import com.ahorra.features.behavior.enums.BehaviorMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BehaviorOverview(
        LocalDate asOf,
        BehaviorMode mode,
        int daysObserved,
        int daysUntilJudgment,
        BigDecimal confidence,
        List<BaselineView> baselines,
        List<HabitView> habits,
        TrendView trend,
        BigDecimal todayZScore,
        Boolean todayUnusual
) {
}
