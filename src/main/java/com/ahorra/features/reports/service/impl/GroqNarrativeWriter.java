package com.ahorra.features.reports.service.impl;

import com.ahorra.common.exception.InternalServerErrorException;
import com.ahorra.features.reports.dto.ReportFacts;
import com.ahorra.features.reports.service.NarrativeWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "report.narrative", name = "provider", havingValue = "groq", matchIfMissing = true)
public class GroqNarrativeWriter implements NarrativeWriter {

    private static final int MAX_COMPLETION_TOKENS = 1024;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final String reasoningEffort;
    public GroqNarrativeWriter(ObjectMapper objectMapper,
                               @Value("${groq.api-key:}") String apiKey,
                               @Value("${groq.narrative-model:openai/gpt-oss-120b}") String model,
                               @Value("${groq.base-url:https://api.groq.com/openai/v1}") String baseUrl,
                               @Value("${groq.narrative-reasoning-effort:low}") String reasoningEffort,
                               @Value("${groq.timeout-seconds:20}") int timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.reasoningEffort = reasoningEffort;
        log.info("Report narrative writer: Groq ({})", model);
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
        body.put("temperature", 0.3);
        body.put("max_completion_tokens", MAX_COMPLETION_TOKENS);
        if (reasoningEffort != null && !reasoningEffort.isBlank()) {
            body.put("reasoning_effort", reasoningEffort.trim());
        }
        ArrayNode messages = body.putArray("messages");
        messages.addObject()
                .put("role", "system")
                .put("content", NarrativePrompt.SYSTEM_PROMPT);
        messages.addObject()
                .put("role", "user")
                .put("content", NarrativePrompt.factsJson(objectMapper, facts).toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(baseUrl + "/chat/completions", HttpMethod.POST, new HttpEntity<>(body.toString(), headers), String.class);
        } catch (RestClientException ex) {
            log.error("Groq narrative call failed: {}", ex.getMessage());
            throw new InternalServerErrorException("Could not reach the narrative writer.", ex);
        }
        JsonNode choice;
        try {
            choice = objectMapper.readTree(response.getBody()).path("choices").path(0);
        } catch (Exception ex) {
            throw new InternalServerErrorException("The narrative writer returned an unreadable result.", ex);
        }
        if ("length".equals(choice.path("finish_reason").asText())) {
            log.warn("Groq narrative cut off at {} tokens; raise the limit or lower the effort", MAX_COMPLETION_TOKENS);
            throw new InternalServerErrorException("The narrative writer ran out of room before it finished.", null);
        }
        JsonNode content = choice.path("message").path("content");
        if (content.isTextual()) {
            String text = clean(content.asText());
            if (!text.isEmpty()) {
                return text;
            }
        }
        throw new InternalServerErrorException("The narrative writer returned no text.", null);
    }

    private static String clean(String raw) {
        String text = raw.trim();
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        return text;
    }
}
