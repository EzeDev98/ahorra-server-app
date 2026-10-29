package com.ahorra.features.behavior.repository;

import com.ahorra.features.behavior.enums.DayType;
import com.ahorra.features.behavior.model.BehaviorBaseline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BehaviorBaselineRepository extends JpaRepository<BehaviorBaseline, Long> {
    Optional<BehaviorBaseline> findByUserIdAndCategoryIdAndWindowDaysAndDayType(Long userId, Long categoryId, int windowDays, DayType dayType);
    List<BehaviorBaseline> findAllByUserId(Long userId);
}
