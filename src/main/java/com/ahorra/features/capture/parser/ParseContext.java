package com.ahorra.features.capture.parser;

import java.time.LocalDate;
import java.util.List;

public record ParseContext(
        String rawText,
        LocalDate today,
        List<String> accountNames,
        List<String> categoryNames
) {
}
