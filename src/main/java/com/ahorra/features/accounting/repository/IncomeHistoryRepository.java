package com.ahorra.features.accounting.repository;

import com.ahorra.features.accounting.model.IncomeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IncomeHistoryRepository extends JpaRepository<IncomeHistory, Long> {
    Optional<IncomeHistory> findByUserIdAndEffectiveToIsNull(Long userId);
    List<IncomeHistory> findAllByUserIdOrderByEffectiveFromDesc(Long userId);

    @Query("""
            select i from IncomeHistory i
            where i.userId = :userId
              and i.effectiveFrom <= :day
              and (i.effectiveTo is null or i.effectiveTo > :day)
            """)
    Optional<IncomeHistory> findActiveOn(@Param("userId") Long userId, @Param("day") LocalDate day);

}
