package com.ahorra.features.income.dto.request;

import java.math.BigDecimal;

public record CategorySpendRow(Long categoryId, BigDecimal amount) {
}
