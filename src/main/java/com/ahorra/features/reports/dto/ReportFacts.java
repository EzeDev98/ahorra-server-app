package com.ahorra.features.reports.dto;

import com.ahorra.features.accounting.enums.PeriodType;
import com.ahorra.features.reports.enums.ReportVerdict;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record ReportFacts(
        LocalDate day,
        int entryCount,
        BigDecimal daySpend,
        PeriodType periodType,
        LocalDate periodStart,
        LocalDate periodEnd,
        int daysElapsed,
        int daysInPeriod,
        BigDecimal baseline,
        BigDecimal incomeMax,
        BigDecimal periodSpend,
        BigDecimal variance,
        BigDecimal rollingAvg,
        int daysObserved,
        boolean comparisonAvailable,
        ReportVerdict verdict,
        String insight,
        boolean goodDay,
        List<CategoryLine> dayCategories,
        List<CategoryLine> periodCategories
) {

    public Set<String> allowedNumbers() {
        Set<String> allowed = new HashSet<>(Set.of("0", "100"));
        List<BigDecimal> values = getValues();
        for (BigDecimal value : values) {
            if (value != null) {
                allowed.add(value.abs().stripTrailingZeros().toPlainString());
            }
        }
        for (int number : new int[]{entryCount, daysElapsed, daysInPeriod, daysObserved, dayCategories.size(), periodCategories.size(), day.getDayOfMonth(), day.getMonthValue(), day.getYear()}) {
            allowed.add(Integer.toString(number));
        }
        return allowed;
    }

    private List<BigDecimal> getValues() {
        List<BigDecimal> values = new ArrayList<>();
        values.add(daySpend);
        values.add(baseline);
        values.add(incomeMax);
        values.add(periodSpend);
        values.add(variance);
        values.add(rollingAvg);
        for (CategoryLine line : dayCategories) {
            values.add(line.amount());
            values.add(line.sharePercent());
            values.add(line.attributedExcess());
        }
        for (CategoryLine line : periodCategories) {
            values.add(line.amount());
            values.add(line.sharePercent());
            values.add(line.attributedExcess());
        }
        return values;
    }
}
