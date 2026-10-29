package com.ahorra.features.behavior.service.impl;

import com.ahorra.features.accounting.enums.AccountType;
import com.ahorra.features.accounting.enums.EntryDirection;
import com.ahorra.features.accounting.model.Category;
import com.ahorra.features.accounting.model.IncomeHistory;
import com.ahorra.features.accounting.repository.CategoryRepository;
import com.ahorra.features.accounting.repository.IncomeHistoryRepository;
import com.ahorra.features.behavior.BehaviorScope;
import com.ahorra.features.behavior.calc.BaselineCalculator;
import com.ahorra.features.behavior.calc.HabitDetector;
import com.ahorra.features.behavior.calc.TrendAnalyzer;
import com.ahorra.features.behavior.dto.CategoryDaySpend;
import com.ahorra.features.behavior.enums.DayType;
import com.ahorra.features.behavior.enums.HabitStatus;
import com.ahorra.features.behavior.model.BehaviorBaseline;
import com.ahorra.features.behavior.model.DetectedHabit;
import com.ahorra.features.behavior.repository.BehaviorBaselineRepository;
import com.ahorra.features.behavior.repository.BehaviorSpendRepository;
import com.ahorra.features.behavior.repository.DetectedHabitRepository;
import com.ahorra.features.behavior.service.BehaviorAnalysisService;
import com.ahorra.features.day.dto.request.DaySpend;
import com.ahorra.features.day.service.impl.DailySpendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.ahorra.common.constants.GeneralConstants.*;
import static com.ahorra.common.constants.GeneralConstants.UNCATEGORIZED;

@Service
public class BehaviorAnalysisServiceImpl implements BehaviorAnalysisService {

    private final DailySpendService dailySpendService;
    private final BehaviorSpendRepository behaviorSpendRepository;
    private final BehaviorBaselineRepository baselineRepository;
    private final DetectedHabitRepository habitRepository;
    private final CategoryRepository categoryRepository;
    private final IncomeHistoryRepository incomeHistoryRepository;
    private final BaselineCalculator baselineCalculator;
    private final HabitDetector habitDetector;
    private final TrendAnalyzer trendAnalyzer;

    public BehaviorAnalysisServiceImpl(DailySpendService dailySpendService,
                                       BehaviorSpendRepository behaviorSpendRepository,
                                       BehaviorBaselineRepository baselineRepository,
                                       DetectedHabitRepository habitRepository,
                                       CategoryRepository categoryRepository,
                                       IncomeHistoryRepository incomeHistoryRepository,
                                       BaselineCalculator baselineCalculator,
                                       HabitDetector habitDetector,
                                       TrendAnalyzer trendAnalyzer) {
        this.dailySpendService = dailySpendService;
        this.behaviorSpendRepository = behaviorSpendRepository;
        this.baselineRepository = baselineRepository;
        this.habitRepository = habitRepository;
        this.categoryRepository = categoryRepository;
        this.incomeHistoryRepository = incomeHistoryRepository;
        this.baselineCalculator = baselineCalculator;
        this.habitDetector = habitDetector;
        this.trendAnalyzer = trendAnalyzer;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refresh(Long userId, LocalDate asOf) {
        LocalDate from = asOf.minusDays(MAX_WINDOW - 1L);
        List<DaySpend> closed = dailySpendService.closedDaySpend(userId, from, asOf);
        if (closed.isEmpty()) {
            return;
        }

        Map<Long, Map<LocalDate, BigDecimal>> byCategory = new HashMap<>();
        for (CategoryDaySpend row : behaviorSpendRepository.sumSpendByDayAndCategory(userId, AccountType.EXPENSE, EntryDirection.DEBIT, from, asOf)) {
            long key = row.categoryId() == null ? BehaviorScope.UNCATEGORIZED : row.categoryId();
            byCategory.computeIfAbsent(key, k -> new HashMap<>())
                    .merge(row.day(), row.amount(), BigDecimal::add);
        }

        Map<Long, List<DaySpend>> series = new LinkedHashMap<>();
        series.put(BehaviorScope.TOTAL, closed);
        byCategory.forEach((key, perDay) -> series.put(key, closed.stream()
                .map(daySpend -> new DaySpend(daySpend.day(), perDay.getOrDefault(daySpend.day(), BigDecimal.ZERO)))
                .toList()));

        int daysObserved = closed.size();
        series.forEach((key, list) -> updateBaselines(userId, key, list, asOf, daysObserved));
        updateHabits(userId, series, asOf);
    }

    private void updateBaselines(Long userId, long categoryId, List<DaySpend> list, LocalDate asOf, int daysObserved) {
        BigDecimal confidence = baselineCalculator.confidence(daysObserved);
        for (int window : WINDOWS) {
            LocalDate windowStart = asOf.minusDays(window - 1L);
            for (DayType dayType : DayType.values()) {
                List<BigDecimal> values = list.stream()
                        .filter(daySpend -> !daySpend.day().isBefore(windowStart) && dayType.matches(daySpend.day()))
                        .map(DaySpend::spend)
                        .toList();
                if (values.isEmpty()) {
                    continue;
                }
                BaselineCalculator.Stats stats = baselineCalculator.stats(values);
                BehaviorBaseline row = baselineRepository
                        .findByUserIdAndCategoryIdAndWindowDaysAndDayType(userId, categoryId, window, dayType)
                        .orElseGet(BehaviorBaseline::new);
                row.setUserId(userId);
                row.setCategoryId(categoryId);
                row.setWindowDays(window);
                row.setDayType(dayType);
                row.setMeanAmount(BaselineCalculator.money(stats.mean()));
                row.setStdDev(BaselineCalculator.money(stats.stdDev()));
                row.setSampleDays(stats.sampleSize());
                row.setConfidence(confidence);
                row.setComputedThrough(asOf);
                baselineRepository.save(row);
            }
        }
    }

    private void updateHabits(Long userId, Map<Long, List<DaySpend>> series, LocalDate asOf) {
        LocalDate habitStart = asOf.minusDays(HABIT_WINDOW - 1L);
        Set<Long> realIds = series.keySet().stream().filter(key -> key > 0).collect(Collectors.toSet());
        Map<Long, String> names = realIds.isEmpty() ? Map.of() : categoryRepository.findAllById(realIds).stream().collect(Collectors.toMap(Category::getId, Category::getName));

        Set<Long> detected = new HashSet<>();
        series.forEach((key, list) -> {
            if (key == BehaviorScope.TOTAL) {
                return;
            }
            List<DaySpend> recent = list.stream().filter(daySpend -> !daySpend.day().isBefore(habitStart)).toList();
            Optional<HabitDetector.HabitSignal> signal = habitDetector.detect(recent);
            if (signal.isEmpty()) {
                return;
            }
            detected.add(key);
            String name = key == BehaviorScope.UNCATEGORIZED ? UNCATEGORIZED : names.getOrDefault(key, UNCATEGORIZED);
            saveHabit(userId, key, name, signal.get());
        });

        for (DetectedHabit existing : habitRepository.findAllByUserId(userId)) {
            if (!detected.contains(existing.getCategoryId()) && existing.getStatus() == HabitStatus.ACTIVE) {
                existing.setStatus(HabitStatus.LAPSED);
                habitRepository.save(existing);
            }
        }
    }

    private void saveHabit(Long userId, long categoryId, String name, HabitDetector.HabitSignal signal) {
        DetectedHabit habit = habitRepository.findByUserIdAndCategoryId(userId, categoryId)
                .orElseGet(() -> {
                    DetectedHabit created = new DetectedHabit();
                    created.setUserId(userId);
                    created.setCategoryId(categoryId);
                    created.setFirstObserved(signal.firstObserved());
                    return created;
                });
        habit.setDescription(String.format(Locale.US, "%s: about %.1f times a week, averaging \u20A6%,.0f", name, signal.frequencyPerWeek().doubleValue(), signal.avgAmount().doubleValue()));
        habit.setLastObserved(signal.lastObserved());
        habit.setFrequencyPerWeek(signal.frequencyPerWeek());
        habit.setAvgAmount(signal.avgAmount());
        habit.setTrendDirection(signal.trend());
        habit.setConfidence(signal.confidence());
        habit.setStatus(HabitStatus.ACTIVE);
        habitRepository.save(habit);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrendAnalyzer.TrendResult> trend(Long userId, LocalDate asOf) {
        Map<LocalDate, BigDecimal> spendByDay = dailySpendService
                .closedDaySpend(userId, asOf.minusDays(55), asOf).stream()
                .collect(Collectors.toMap(DaySpend::day, DaySpend::spend));

        List<Double> weeklyRatioPercents = new ArrayList<>();
        for (int weeksAgo = 7; weeksAgo >= 0; weeksAgo--) {
            LocalDate weekEnd = asOf.minusDays(7L * weeksAgo);
            LocalDate weekStart = weekEnd.minusDays(6);

            BigDecimal weekSpend = BigDecimal.ZERO;
            boolean weekHasAllDays = true;
            for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
                BigDecimal daySpend = spendByDay.get(date);
                if (daySpend == null) {
                    weekHasAllDays = false;
                    break;
                }
                weekSpend = weekSpend.add(daySpend);
            }
            if (!weekHasAllDays) {
                continue;
            }

            Optional<IncomeHistory> incomeHistory = incomeHistoryRepository.findActiveOn(userId, weekEnd);
            if (incomeHistory.isEmpty()) {
                continue;
            }
            double weeklyBaselineAmount = weeklyIncomeBaseline(incomeHistory.get());
            if (weeklyBaselineAmount <= 0) {
                continue;
            }

            weeklyRatioPercents.add(weekSpend.doubleValue() / weeklyBaselineAmount * 100.0);
        }
        return trendAnalyzer.analyze(weeklyRatioPercents);
    }

    private double weeklyIncomeBaseline(IncomeHistory income) {
        double periodDays = switch (income.getPeriodType()) {
            case DAILY -> 1;
            case WEEKLY -> 7;
            case MONTHLY -> 365.25 / 12;
            case CUSTOM -> income.getPeriodLength();
        };
        return income.getMinAmount().doubleValue() / periodDays * 7;
    }
}
