package com.ahorra.features.behavior.calc;

import com.ahorra.features.behavior.enums.TrendDirection;
import com.ahorra.features.day.dto.request.DaySpend;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class HabitDetector {

    static final int MIN_DAYS = 21;
    static final double MIN_PER_WEEK = 2.0;
    static final double MAX_VARIATION = 0.5;
    static final double MIN_WEEK_COVERAGE = 0.75;
    static final double TREND_CHANGE = 0.15;

    private static final int DAYS_PER_WEEK = 7;

    public record HabitSignal(BigDecimal frequencyPerWeek,
                              BigDecimal avgAmount,
                              LocalDate firstObserved,
                              LocalDate lastObserved,
                              TrendDirection trend,
                              BigDecimal confidence) {
    }

    public Optional<HabitSignal> detect(List<DaySpend> series) {
        int totalDays = series.size();
        if (totalDays < MIN_DAYS) {
            return Optional.empty();
        }
        List<DaySpend> activeDays = series.stream()
                .filter(day -> day.spend().signum() > 0)
                .toList();
        if (activeDays.size() < 2) {
            return Optional.empty();
        }

        double frequencyPerWeek = activeDays.size() / (totalDays / (double) DAYS_PER_WEEK);
        if (frequencyPerWeek < MIN_PER_WEEK) {
            return Optional.empty();
        }

        double meanActiveSpend = activeDays.stream()
                .mapToDouble(day -> day.spend().doubleValue())
                .average()
                .orElse(0);
        if (meanActiveSpend <= 0) {
            return Optional.empty();
        }
        double sumOfSquaredDeviations = 0;
        for (DaySpend activeDay : activeDays) {
            double deviation = activeDay.spend().doubleValue() - meanActiveSpend;
            sumOfSquaredDeviations += deviation * deviation;
        }
        double sampleStdDev = Math.sqrt(sumOfSquaredDeviations / (activeDays.size() - 1));
        double coefficientOfVariation = sampleStdDev / meanActiveSpend;
        if (coefficientOfVariation > MAX_VARIATION) {
            return Optional.empty();
        }

        int weekCount = totalDays / DAYS_PER_WEEK;
        List<DaySpend> daysInWholeWeeks = series.subList(totalDays - weekCount * DAYS_PER_WEEK, totalDays);
        double[] weeklySpend = new double[weekCount];
        int weeksWithSpend = 0;
        for (int weekIndex = 0; weekIndex < weekCount; weekIndex++) {
            boolean hasSpendThisWeek = false;
            for (int dayOfWeek = 0; dayOfWeek < DAYS_PER_WEEK; dayOfWeek++) {
                BigDecimal daySpend = daysInWholeWeeks.get(weekIndex * DAYS_PER_WEEK + dayOfWeek).spend();
                weeklySpend[weekIndex] += daySpend.doubleValue();
                if (daySpend.signum() > 0) {
                    hasSpendThisWeek = true;
                }
            }
            if (hasSpendThisWeek) {
                weeksWithSpend++;
            }
        }
        double weekCoverage = weeksWithSpend / (double) weekCount;
        if (weekCoverage < MIN_WEEK_COVERAGE) {
            return Optional.empty();
        }

        TrendDirection trend = getTrendDirection(weekCount, weeklySpend);

        double confidence = Math.min(totalDays / (double) BaselineCalculator.FULL_CONFIDENCE_DAYS, 1.0)
                * weekCoverage;

        return Optional.of(new HabitSignal(
                BigDecimal.valueOf(frequencyPerWeek).setScale(2, RoundingMode.HALF_UP),
                BaselineCalculator.money(meanActiveSpend),
                activeDays.get(0).day(),
                activeDays.get(activeDays.size() - 1).day(),
                trend,
                BigDecimal.valueOf(confidence).setScale(3, RoundingMode.HALF_UP)));
    }

    private static TrendDirection getTrendDirection(int weekCount, double[] weeklySpend) {
        int weeksPerHalf = weekCount / 2;
        double firstHalfSpend = 0;
        double secondHalfSpend = 0;
        for (int offset = 0; offset < weeksPerHalf; offset++) {
            firstHalfSpend += weeklySpend[offset];
            secondHalfSpend += weeklySpend[weekCount - 1 - offset];
        }
        TrendDirection trend;
        if (firstHalfSpend <= 0) {
            trend = secondHalfSpend > 0 ? TrendDirection.WORSENING : TrendDirection.STABLE;
        } else {
            double relativeChange = (secondHalfSpend - firstHalfSpend) / firstHalfSpend;
            trend = relativeChange > TREND_CHANGE ? TrendDirection.WORSENING
                    : relativeChange < -TREND_CHANGE ? TrendDirection.IMPROVING
                    : TrendDirection.STABLE;
        }
        return trend;
    }
}
