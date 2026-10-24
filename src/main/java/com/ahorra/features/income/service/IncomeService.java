package com.ahorra.features.income.service;

import com.ahorra.features.income.dto.request.IncomeRequest;
import com.ahorra.features.income.dto.response.IncomeResponse;

import java.util.List;

public interface IncomeService {
    IncomeResponse getCurrentIncome(Long userId);
    List<IncomeResponse> getHistory(Long userId);
    IncomeResponse setIncome(Long userId, IncomeRequest request);
}
