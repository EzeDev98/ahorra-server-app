package com.ahorra.features.capture.dto.request;

import java.util.List;

public record ParsedEntry(
        String flow,
        String entryDate,
        String vendor,
        String categoryName,
        String accountName,
        List<ParsedItem> items
) {
}
