package com.ahorra.features.capture.parser;

import com.ahorra.features.capture.dto.request.ParsedEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

class EntryListSupport {

    private EntryListSupport() {
    }

    static com.fasterxml.jackson.databind.node.ObjectNode entriesSchema(ObjectMapper objectMapper, ObjectNode entrySchema) {
        return wrap(objectMapper, entrySchema, UnaryOperator.identity());
    }

    static ObjectNode entriesSchema(ObjectMapper objectMapper, UnaryOperator<String> typeName) {
        return wrap(objectMapper, EntryParserSupport.entrySchema(objectMapper, typeName), typeName);
    }

    private static ObjectNode wrap(ObjectMapper objectMapper, ObjectNode entrySchema, UnaryOperator<String> typeName) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", typeName.apply("object"));
        ObjectNode entries = schema.putObject("properties").putObject("entries");
        entries.put("type", typeName.apply("array"));
        entries.put("description", "One element per separate spend or income event, in the order they were mentioned.");
        entries.set("items", entrySchema);
        schema.putArray("required").add("entries");
        return schema;
    }

    static List<ParsedEntry> toParsedEntries(JsonNode input) {
        List<ParsedEntry> parsed = new ArrayList<>();
        JsonNode entries = input.path("entries");
        if (entries.isArray()) {
            for (JsonNode entry : entries) {
                parsed.add(EntryParserSupport.toParsedEntry(entry));
            }
        } else if(input.has("items")) {
            parsed.add(EntryParserSupport.toParsedEntry(input));
        }

        return parsed;
    }
}
