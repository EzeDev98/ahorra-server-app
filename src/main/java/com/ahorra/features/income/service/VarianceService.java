package com.ahorra.features.income.service;

import com.ahorra.features.income.dto.response.VarianceResponse;

import java.time.LocalDate;

public interface VarianceService {
    VarianceResponse variance(Long userId, LocalDate asOf);
}
