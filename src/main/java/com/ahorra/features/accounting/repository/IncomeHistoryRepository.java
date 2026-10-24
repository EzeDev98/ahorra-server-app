package com.ahorra.features.accounting.repository;

import com.ahorra.features.accounting.model.IncomeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncomeHistoryRepository extends JpaRepository<IncomeHistory, Long> {
    Optional<IncomeHistory> findByUserIdAndEffectiveToIsNull(Long userId);
    List<IncomeHistory> findAllByUserIdOrderByEffectiveFromDesc(Long userId);
}
