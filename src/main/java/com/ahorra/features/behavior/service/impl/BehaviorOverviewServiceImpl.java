package com.ahorra.features.behavior.service.impl;

import com.ahorra.features.accounting.enums.AccountType;
import com.ahorra.features.accounting.enums.EntryDirection;
import com.ahorra.features.accounting.model.Category;
import com.ahorra.features.accounting.repository.CategoryRepository;
import com.ahorra.features.accounting.repository.LedgerEntryRepository;
import com.ahorra.features.behavior.BehaviorScope;
import com.ahorra.features.behavior.calc.BaselineCalculator;
import com.ahorra.features.behavior.calc.TrendAnalyzer.TrendResult;
import com.ahorra.features.behavior.dto.BaselineView;
import com.ahorra.features.behavior.dto.BehaviorOverview;
import com.ahorra.features.behavior.dto.HabitView;
import com.ahorra.features.behavior.dto.TrendView;
import com.ahorra.features.behavior.enums.BehaviorMode;
import com.ahorra.features.behavior.enums.DayType;
import com.ahorra.features.behavior.enums.HabitStatus;
import com.ahorra.features.behavior.enums.TrendDirection;
import com.ahorra.features.behavior.model.BehaviorBaseline;
import com.ahorra.features.behavior.repository.BehaviorBaselineRepository;
import com.ahorra.features.behavior.repository.DetectedHabitRepository;
import com.ahorra.features.behavior.service.BehaviorAnalysisService;
import com.ahorra.features.behavior.service.BehaviorOverviewService;
import com.ahorra.features.day.dto.request.DaySpend;
import com.ahorra.features.day.service.impl.DailySpendService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.ahorra.common.constants.GeneralConstants.*;

@Service
public class BehaviorOverviewServiceImpl implements BehaviorOverviewService {

    private final DailySpendService dailySpendService;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final BehaviorBaselineRepository baselineRepository;
    private final DetectedHabitRepository habitRepository;
    private final CategoryRepository categoryRepository;
    private final BehaviorAnalysisService analysisService;
    private final BaselineCalculator baselineCalculator;
    private final ZoneId zone;

    public BehaviorOverviewServiceImpl(DailySpendService dailySpendService,
                                       LedgerEntryRepository ledgerEntryRepository,
                                       BehaviorBaselineRepository baselineRepository,
                                       DetectedHabitRepository habitRepository,
                                       CategoryRepository categoryRepository,
                                       BehaviorAnalysisService analysisService,
                                       BaselineCalculator baselineCalculator,
                                       @Value("${app.timezone:Africa/Lagos}") String timezone) {
        this.dailySpendService = dailySpendService;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.baselineRepository = baselineRepository;
        this.habitRepository = habitRepository;
        this.categoryRepository = categoryRepository;
        this.analysisService = analysisService;
        this.baselineCalculator = baselineCalculator;
        this.zone = ZoneId.of(timezone);
    }

    @Override
    @Transactional(readOnly = true)
    public BehaviorOverview overview(Long userId) {
        LocalDate today = LocalDate.now(zone);
        LocalDate lastClosed = today.minusDays(1);

        List<DaySpend> closed = dailySpendService.closedDaySpend(userId, today.minusDays(LOOKBACK_DAYS), lastClosed);
        int daysObserved = closed.size();
        BehaviorMode mode = daysObserved < JUDGMENT_DAYS ? BehaviorMode.OBSERVATION : BehaviorMode.JUDGEMENT;
        int daysUntil = Math.max(0, JUDGMENT_DAYS - daysObserved);
        BigDecimal confidence = baselineCalculator.confidence(daysObserved);

        List<BehaviorBaseline> baselineRows = baselineRepository.findAllByUserId(userId);
        Map<Long, String> names = categoryNames(baselineRows);
        List<BaselineView> baselines = baselineRows.stream()
                .sorted(Comparator.comparing((BehaviorBaseline baseline) -> baseline.getCategoryId() != BehaviorScope.TOTAL)
                        .thenComparing(BehaviorBaseline::getCategoryId)
                        .thenComparing(BehaviorBaseline::getWindowDays)
                        .thenComparing(BehaviorBaseline::getDayType))
                .map(baseline -> new BaselineView(
                        nameOf(baseline.getCategoryId(), names), baseline.getWindowDays(), baseline.getDayType(),
                        baseline.getMeanAmount(), baseline.getStdDev(), baseline.getSampleDays(),
                        baseline.getConfidence(), baseline.getComputedThrough()))
                .toList();

        if (mode == BehaviorMode.OBSERVATION) {
            return new BehaviorOverview(today, mode, daysObserved, daysUntil, confidence,
                    baselines, List.of(), null, null, null);
        }

        List<HabitView> habits = habitRepository.findAllByUserIdAndStatus(userId, HabitStatus.ACTIVE).stream()
                .sorted(Comparator.comparing(habit -> habit.getConfidence(), Comparator.reverseOrder()))
                .map(habit -> new HabitView(habit.getDescription(), habit.getFrequencyPerWeek(), habit.getAvgAmount(),
                        habit.getFirstObserved(), habit.getLastObserved(), habit.getTrendDirection(), habit.getConfidence()))
                .toList();

        TrendView trend = analysisService.trend(userId, lastClosed)
                .map(this::toView)
                .orElse(null);

        BigDecimal zScore = null;
        Boolean unusual = null;
        Optional<BehaviorBaseline> usual = baselineRepository
                .findByUserIdAndCategoryIdAndWindowDaysAndDayType(userId, BehaviorScope.TOTAL, 30, dayTypeOf(today));
        if (usual.isPresent()) {
            BigDecimal spendToday = ledgerEntryRepository
                    .sumSpendByDay(userId, AccountType.EXPENSE, EntryDirection.DEBIT, today, today)
                    .stream().map(DaySpend::spend).reduce(BigDecimal.ZERO, BigDecimal::add);
            BehaviorBaseline baseline = usual.get();
            Double z = baselineCalculator.zScore(spendToday, new BaselineCalculator.Stats(baseline.getMeanAmount().doubleValue(), baseline.getStdDev().doubleValue(), baseline.getSampleDays()));
            if (z != null) {
                zScore = BaselineCalculator.money(z);
                unusual = z >= UNUSUAL_Z;
            }
        }

        return new BehaviorOverview(today, mode, daysObserved, daysUntil, confidence, baselines, habits, trend, zScore, unusual);
    }

    private TrendView toView(TrendResult result) {
        boolean above = result.direction() == TrendDirection.WORSENING && result.latestRatioPercent().compareTo(HUNDRED) >= 0;
        return new TrendView(result.direction(), result.slopePerWeek(), result.latestRatioPercent(), result.weeksUsed(), above);
    }

    private DayType dayTypeOf(LocalDate day) {
        return DayType.WEEKEND.matches(day) ? DayType.WEEKEND : DayType.WEEKDAY;
    }

    private Map<Long, String> categoryNames(List<BehaviorBaseline> rows) {
        Set<Long> ids = rows.stream().map(BehaviorBaseline::getCategoryId)
                .filter(id -> id > 0).collect(Collectors.toSet());
        return ids.isEmpty() ? Map.of()
                : categoryRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    private String nameOf(Long categoryId, Map<Long, String> names) {
        if (categoryId == BehaviorScope.TOTAL) {
            return ALL_SPENDING;
        }
        if (categoryId == BehaviorScope.UNCATEGORIZED) {
            return UNCATEGORIZED;
        }
        return names.getOrDefault(categoryId, UNCATEGORIZED);
    }
}
