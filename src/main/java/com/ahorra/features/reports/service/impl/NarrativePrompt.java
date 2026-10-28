package com.ahorra.features.reports.service.impl;
import com.ahorra.features.reports.dto.CategoryLine;
import com.ahorra.features.reports.dto.ReportFacts;
import com.ahorra.features.reports.enums.ReportVerdict;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

final class NarrativePrompt {

    static final String SYSTEM_PROMPT = """
            You write one short paragraph (at most four sentences) summarising a person's spending day \
            for their personal finance app. The user message is a JSON object of facts.
            Rules:
            - Use only the facts given. Copy every amount and percentage exactly as written. \
            Never calculate, round, estimate or invent a number.
            - Write no number that is not in the facts. Never write a number of days. \
            Say "recently" or "lately" instead.
            - Do not mention dates.
            - Be descriptive and neutral: state the pattern and the figure and let the reader draw \
            their own conclusion. No advice, no scolding.
            - Only if goodDay is true, include one specific, factual acknowledgement of what went well.
            - Output only the paragraph.""";

    private NarrativePrompt() {
    }

    static ObjectNode factsJson(ObjectMapper objectMapper, ReportFacts facts) {
        ObjectNode nodes = objectMapper.createObjectNode();
        nodes.put("entriesToday", facts.entryCount());
        nodes.put("spendToday", ReportFormat.money(facts.daySpend()));
        ArrayNode dayCategories = nodes.putArray("spendTodayByCategory");
        for (CategoryLine line : facts.dayCategories()) {
            ObjectNode category = dayCategories.addObject();
            category.put("category", line.name());
            category.put("amount", ReportFormat.money(line.amount()));
            category.put("shareOfToday", ReportFormat.percent(line.sharePercent()));
        }
        if (facts.verdict() != ReportVerdict.NO_INCOME_SET) {
            nodes.put("periodType", facts.periodType().name().toLowerCase());
            nodes.put("spentSoFarThisPeriod", ReportFormat.money(facts.periodSpend()));
            nodes.put("incomeBaseline", ReportFormat.money(facts.baseline()));
            nodes.put("incomeUpperEnd", ReportFormat.money(facts.incomeMax()));
            nodes.put("differenceFromBaseline", ReportFormat.money(facts.variance()));
            nodes.put("differenceDirection",
                    facts.variance().signum() > 0 ? "above the baseline" : "below the baseline");
            nodes.put("paceAssessment", facts.verdict().name());
        }
        if (facts.comparisonAvailable() && facts.rollingAvg() != null) {
            nodes.put("usualDailySpendRecently", ReportFormat.money(facts.rollingAvg()));
        }
        if (facts.insight() != null) {
            nodes.put("keyInsight", facts.insight());
        }
        nodes.put("goodDay", facts.goodDay());
        return nodes;
    }
}