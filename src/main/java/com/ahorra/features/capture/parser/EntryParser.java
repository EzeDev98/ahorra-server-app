package com.ahorra.features.capture.parser;

import com.ahorra.features.capture.dto.request.ParsedEntry;

import java.util.List;

public interface EntryParser {
    List<ParsedEntry> parse(ParseContext context);
}
