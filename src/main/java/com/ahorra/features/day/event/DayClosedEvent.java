package com.ahorra.features.day.event;

import java.time.LocalDate;

public record DayClosedEvent(
        Long userId,
        LocalDate day,
        boolean retroactive
) {
}
