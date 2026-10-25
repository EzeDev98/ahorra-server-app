package com.ahorra.features.capture.parser;

import com.ahorra.common.exception.InternalServerErrorException;
import com.ahorra.features.capture.dto.request.ParsedEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.ahorra.common.constants.GeneralConstants.TOOL_NAME;

@Slf4j
@Component
public class GroqEntryParser implements EntryParser{

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public GroqEntryParser(ObjectMapper objectMapper,
                           RestTemplate restTemplate,
                           @Value("${groq.api-key:}")
                           String apiKey,
                           @Value("${groq.model:openai/gpt-oss-120b}")
                           String model,
                           @Value("${groq.base-url:https://api.groq.com/openai/v1}")
                           String baseUrl,
                           @Value("${groq.timeout-seconds:20}") int timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.restTemplate = EntryParserSupport.newRestTemplate(timeoutSeconds);
    }


    private static ObjectNode allowNullOptionals(ObjectNode schema) {
        JsonNode required = schema.path("required");
        ObjectNode properties = (ObjectNode) schema.get("properties");
        properties.fieldNames().forEachRemaining(name -> {
            boolean isRequired = false;
            for (JsonNode require : required) {
                if (require.asText().equals(name)) {
                    isRequired = true;
                }
            }

            JsonNode type = properties.get(name).get("type");
            if (!isRequired && type !=null && type.isTextual() && type.asText().equals("String")) {
                ((ObjectNode) properties.get(name)).putArray("type").add("String").add("null");
            }
        });
        return schema;
    }

    @Override
    public List<ParsedEntry> parse(ParseContext context) {
        EntryParserSupport.requireKey(apiKey);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(apiKey);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(baseUrl + "/chat/completions", HttpMethod.POST, new HttpEntity<>(buildRequestBody(context), httpHeaders), String.class);
        } catch (RestClientException ex) {
            throw EntryParserSupport.callFailed("Groq", ex);
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode arguments = root.path("choices").path(0).path("message").path("tool_calls").path(0).path("function").path("arguments");
            JsonNode input = EntryParserSupport.readArguments(objectMapper, arguments);
            if (input != null) {
                return EntryListSupport.toParsedEntries(input);
            }
        } catch (Exception ex) {
            log.error("Could not read groq response", ex);
            throw new InternalServerErrorException("The entry parser returned no result.", null);
        }

        throw new InternalServerErrorException("The entry parser returned no result.", null);
    }

    private String buildRequestBody(ParseContext context) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("temperature", 0);
        body.put("max_tokens", 1024);

        ArrayNode messages = body.putArray("messages");
        messages.addObject()
                .put("role", "system")
                .put("content", EntryParserSupport.systemPrompt(context));
        messages.addObject()
                .put("role", "user")
                .put("content", context.rawText());

        ObjectNode function = objectMapper.createObjectNode();
        function.put("name", TOOL_NAME);
        function.put("description", EntryParserSupport.TOOL_DESCRIPTION);
        function.set("parameters", EntryListSupport.entriesSchema(objectMapper, allowNullOptionals(EntryParserSupport.entrySchema(objectMapper))));
        body.putArray("tools").addObject()
                .put("type", "function")
                .set("function", function);

        ObjectNode toolChoice = body.putObject("tool_choice");
        toolChoice.put("type", "function");
        toolChoice.putObject("function").put("name", TOOL_NAME);

        return body.toString();
    }
}
