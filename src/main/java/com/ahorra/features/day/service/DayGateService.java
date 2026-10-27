package com.ahorra.features.day.service;

import com.ahorra.features.day.dto.request.DayGate;
import com.ahorra.features.day.dto.response.DayStatusResponse;

public interface DayGateService {
    DayGate evaluate(Long userId);
    void assertNotGated(Long userId);
    DayStatusResponse status(Long userId);
}
