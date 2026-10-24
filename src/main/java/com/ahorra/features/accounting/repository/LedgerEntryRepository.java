package com.ahorra.features.accounting.repository;

import com.ahorra.features.accounting.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    Optional<LedgerEntry> findByIdAndUserId(Long id, Long userId);
    List<LedgerEntry> findAllByUserIdAndEntryDate(Long userId, LocalDate entryDate);
    long countByUserIdAndEntryDate(Long userId, LocalDate entryDate);
}
