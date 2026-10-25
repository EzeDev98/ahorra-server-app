package com.ahorra.features.capture.dto.response;

import com.ahorra.features.capture.dto.request.ParsedItem;
import com.ahorra.features.capture.enums.FlowDirection;
import com.ahorra.features.capture.enums.PendingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PendingEntryResponse(
        Long id,
        Long rawEntryId,
        PendingStatus status,
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
