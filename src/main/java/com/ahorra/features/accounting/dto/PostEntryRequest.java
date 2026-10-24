package com.ahorra.features.accounting.dto;

import java.time.LocalDate;
import java.util.List;

public record PostEntryRequest(
        Long userId,
        Long categoryId,
        LocalDate entryDate,
        String description,
        List<PostEntryLine> lines
) {
}
