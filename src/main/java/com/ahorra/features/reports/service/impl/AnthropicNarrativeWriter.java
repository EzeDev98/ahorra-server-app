package com.ahorra.features.reports.service.impl;

import com.ahorra.common.exception.InternalServerErrorException;
import com.ahorra.features.reports.dto.CategoryLine;
import com.ahorra.features.reports.dto.ReportFacts;
import com.ahorra.features.reports.enums.ReportVerdict;
import com.ahorra.features.reports.service.NarrativeWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Component
public class AnthropicNarrativeWriter implements NarrativeWriter {

    private static final String API_VERSION = "2023-06-01";

    private static final String SYSTEM_PROMPT = """
            You write one short paragraph (at most four sentences) summarising a person's spending day \
            for their personal finance app. The user message is a JSON object of facts.
            Rules:
            - Use only the facts given. Copy every amount and percentage exactly as written. \
            Never calculate, round, estimate or invent a number.
            - Do not mention dates.
            - Be descriptive and neutral: state the pattern and the figure and let the reader draw \
            their own conclusion. No advice, no scolding.
            - Only if goodDay is true, include one specific, factual acknowledgement of what went well.
            - Output only the paragraph.""";

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public AnthropicNarrativeWriter(ObjectMapper objectMapper,
                                    @Value("${anthropic.api-key:}") String apiKey,
                                    @Value("${anthropic.model:claude-haiku-4-5}") String model,
                                    @Value("${anthropic.base-url:https://api.anthropic.com}") String baseUrl,
                                    @Value("${anthropic.timeout-seconds:20}") int timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public String write(ReportFacts facts) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new InternalServerErrorException("Report narrative is not configured", null);
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("max_tokens", 400);
        body.put("system", SYSTEM_PROMPT);
        ArrayNode messages = body.putArray("messages");
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", factsJson(facts).toString());

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", API_VERSION);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(baseUrl + "/v1/messages", HttpMethod.POST, new HttpEntity<>(body.toString(), headers), String.class);
        } catch (RestClientException ex) {
            log.error("Anthropic narrative call failed: {}", ex.getMessage());
            throw new InternalServerErrorException("Could not reach the narrative writer.", ex);
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            for (JsonNode block : root.path("content")) {
                if ("text".equals(block.path("type").asText())) {
                    String text = block.path("text").asText().trim();
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            }
        } catch (Exception ex) {
            throw new InternalServerErrorException("The narrative writer returned an unreadable result.", ex);
        }
        throw new InternalServerErrorException("The narrative writer returned no text.", null);
    }

    private ObjectNode factsJson(ReportFacts facts) {
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
            nodes.put("differenceDirection", facts.variance().signum() > 0 ? "above the baseline" : "below the baseline");
            nodes.put("paceAssessment", facts.verdict().name());
        }
        if (facts.comparisonAvailable() && facts.rollingAvg() != null) {
            nodes.put("usualDailySpendLast30Days", ReportFormat.money(facts.rollingAvg()));
        }
        if (facts.insight() != null) {
            nodes.put("keyInsight", facts.insight());
        }
        nodes.put("goodDay", facts.goodDay());
        return nodes;
    }
}
