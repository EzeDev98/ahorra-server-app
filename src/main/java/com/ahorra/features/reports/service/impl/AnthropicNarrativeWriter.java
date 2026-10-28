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
@ConditionalOnProperty(prefix = "report.narrative", name = "provider", havingValue = "anthropic")
public class AnthropicNarrativeWriter implements NarrativeWriter {

    private static final String API_VERSION = "2023-06-01";
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    public AnthropicNarrativeWriter(ObjectMapper objectMapper,
                                    @Value("${anthropic.api-key:}") String apiKey,
                                    @Value("${anthropic.model:claude-haiku-4-5}") String model,
                                    @Value("${anthropic.base-url:https://api.anthropic.com}")
                                    String baseUrl,
                                    @Value("${anthropic.timeout-seconds:20}") int timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        log.info("Report narrative writer: Anthropic ({})", model);
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
        body.put("system", NarrativePrompt.SYSTEM_PROMPT);
        ArrayNode messages = body.putArray("messages");
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", NarrativePrompt.factsJson(objectMapper, facts).toString());
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", API_VERSION);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(baseUrl + "/v1/messages", HttpMethod.POST,
                    new HttpEntity<>(body.toString(), headers), String.class);
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
            throw new InternalServerErrorException(
                    "The narrative writer returned an unreadable result.", ex);
        }
        throw new InternalServerErrorException("The narrative writer returned no text.", null);
    }

}
