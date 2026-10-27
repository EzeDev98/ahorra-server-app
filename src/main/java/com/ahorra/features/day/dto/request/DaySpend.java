package com.ahorra.features.day.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DaySpend(
        LocalDate day,
        BigDecimal spend
) {
}
