package com.ahorra.features.reports.repository;

import com.ahorra.features.reports.model.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {
    boolean existsByUserIdAndReportDate(Long userId, LocalDate reportDate);
    Optional<DailyReport> findByUserIdAndReportDate(Long userId, LocalDate reportDate);
    Optional<DailyReport> findFirstByUserIdOrderByReportDateDesc(Long userId);
    List<DailyReport> findByUserIdAndReportDateBetweenOrderByReportDateDesc(Long userId, LocalDate from, LocalDate to);
}
