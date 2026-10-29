package com.ahorra.features.behavior.calc;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class BaselineCalculator {

    public static final int FULL_CONFIDENCE_DAYS = 30;

    public record Stats(double mean, double stdDev, int sampleSize) {
    }

    public Stats stats(List<BigDecimal> values) {
        int sampleSize = values.size();
        if (sampleSize == 0) {
            return new Stats(0, 0, 0);
        }
        double mean = values.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0);
        if (sampleSize < 2) {
            return new Stats(mean, 0, sampleSize);
        }
        double sumSquares = 0;
        for (BigDecimal value : values) {
            double deviation = value.doubleValue() - mean;
            sumSquares += deviation * deviation;
        }
        return new Stats(mean, Math.sqrt(sumSquares / (sampleSize - 1)), sampleSize);
    }

    public BigDecimal confidence(int daysObserved) {
        double confidenceRatio = Math.min(daysObserved / (double) FULL_CONFIDENCE_DAYS, 1.0);
        return BigDecimal.valueOf(confidenceRatio).setScale(3, RoundingMode.HALF_UP);
    }

    public Double zScore(BigDecimal amount, Stats stats) {
        if (stats.sampleSize() < 2 || stats.stdDev() <= 0) {
            return null;
        }
        return (amount.doubleValue() - stats.mean()) / stats.stdDev();
    }

    public static BigDecimal money(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
