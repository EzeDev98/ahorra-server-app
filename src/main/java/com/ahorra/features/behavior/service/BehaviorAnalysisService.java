package com.ahorra.features.behavior.service;


import com.ahorra.features.behavior.calc.TrendAnalyzer.TrendResult;

import java.time.LocalDate;
import java.util.Optional;

public interface BehaviorAnalysisService {
    void refresh(Long userId, LocalDate asOf);
    Optional<TrendResult> trend(Long userId, LocalDate asOf);
}
