package com.ahorra.features.reports.service.impl;

import com.ahorra.common.money.MoneyUtils;
import com.ahorra.features.reports.dto.CategoryLine;
import com.ahorra.features.reports.dto.ReportFacts;
import com.ahorra.features.reports.enums.ReportVerdict;
import org.springframework.stereotype.Component;

@Component
public class TemplateNarrative {

    public String write(ReportFacts facts) {
        StringBuilder stringBuilder = new StringBuilder();

        if (MoneyUtils.isPositive(facts.daySpend())) {
            stringBuilder.append("Confirmed spending for the day was ").append(ReportFormat.money(facts.daySpend()))
                    .append(" across ").append(facts.entryCount()).append(facts.entryCount() == 1 ? " entry" : " entries");
            if (!facts.dayCategories().isEmpty()) {
                CategoryLine top = facts.dayCategories().get(0);
                stringBuilder.append(", with ").append(top.name()).append(" the largest at ")
                        .append(ReportFormat.money(top.amount())).append(" (")
                        .append(ReportFormat.percent(top.sharePercent())).append(" of the day)");
            }
            stringBuilder.append(". ");
        } else {
            stringBuilder.append("The day's ").append(facts.entryCount()).append(facts.entryCount() == 1 ? " entry was" : " entries were")
                    .append(" not spending, so no spending was recorded. ");
        }

        if (facts.verdict() != ReportVerdict.NO_INCOME_SET) {
            boolean above = MoneyUtils.isPositive(facts.variance());
            stringBuilder.append("So far this ").append(periodLabel(facts)).append(" you have spent ")
                    .append(ReportFormat.money(facts.periodSpend())).append(" against an income baseline of ")
                    .append(ReportFormat.money(facts.baseline())).append(" (range ")
                    .append(ReportFormat.money(facts.baseline())).append(" to ")
                    .append(ReportFormat.money(facts.incomeMax())).append("), which is ")
                    .append(ReportFormat.money(facts.variance())).append(above ? " above" : " below")
                    .append(" the baseline. ");
        }

        if (facts.comparisonAvailable() && facts.rollingAvg() != null) {
            stringBuilder.append("Your usual daily spend over the last 30 days is ")
                    .append(ReportFormat.money(facts.rollingAvg())).append(". ");
        }
        if (facts.goodDay()) {
            stringBuilder.append("Spending is running below the pace your income baseline implies. ");
        }

        String text = stringBuilder.toString().trim();
        return text.length() > 900 ? text.substring(0, 900) : text;
    }

    private String periodLabel(ReportFacts f) {
        return switch (f.periodType()) {
            case DAILY -> "day";
            case WEEKLY -> "week";
            case MONTHLY -> "month";
            case CUSTOM -> "period";
        };
    }
}
