package com.ahorra.features.day.dto.response;

import com.ahorra.features.capture.dto.response.PendingEntryResponse;
import com.ahorra.features.day.enums.GateReason;

import java.time.LocalDate;
import java.util.List;

public record DayStatusResponse(
        boolean gated,
        GateReason reason,
        LocalDate day,
        int othersDayRemaining,
        String hardGateTime,
        List<PendingEntryResponse> pending
) {
}
