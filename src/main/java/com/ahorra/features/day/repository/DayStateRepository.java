package com.ahorra.features.day.repository;

import com.ahorra.features.day.enums.DayStatus;
import com.ahorra.features.day.model.DayState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DayStateRepository extends JpaRepository<DayState, Long> {
    Optional<DayState> findTopByUserIdOrderByDayDateDesc(Long userId);
    List<DayState> findAllByUserIdAndStatus(Long userId, DayStatus status);
    boolean existsByUserIdAndDayDateAndStatus(Long userId, LocalDate dayDate, DayStatus status);
    List<DayState> findAllByUserIdAndStatusAndDayDateBetweenOrderByDayDateAsc(Long userId, DayStatus status, LocalDate from, LocalDate to);

    @Query("""
            select status.dayDate from DayState status where status.userId = :userId and status.status = :status and not exists (select 1 from dailyReport report
            where report.userId = status.userId and report.reportDate = status.dayDate) order by status.dayDate asc""")
    List<LocalDate> findDaysWithoutReport(@Param("userId") Long userId, @Param("status") DayStatus status);
}
