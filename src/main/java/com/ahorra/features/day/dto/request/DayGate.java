package com.ahorra.features.day.dto.request;

import com.ahorra.features.day.enums.GateReason;

import java.time.LocalDate;

public record DayGate(
        boolean gated,
        GateReason reason,
        LocalDate day,
        int otherDaysRemaining
) {
    public static DayGate open() {
        return new DayGate(false, GateReason.NONE, null, 0);
    }
}
