package com.ahorra.features.accounting.dto;

import com.ahorra.features.accounting.enums.EntryDirection;

import java.math.BigDecimal;

public record PostEntryLine(Long accountId, EntryDirection direction, BigDecimal amount) {
}
