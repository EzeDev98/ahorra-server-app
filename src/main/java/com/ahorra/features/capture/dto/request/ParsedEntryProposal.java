package com.ahorra.features.capture.dto.request;

import com.ahorra.features.capture.enums.FlowDirection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ParsedEntryProposal(
        Long rawEntryId,
        FlowDirection flow,
        LocalDate entryDate,
        String vendor,
        Long categoryId,
        String categoryName,
        Long accountId,
        String accountName,
        List<ParsedItem> items,
        BigDecimal totalAmount
) {
}
