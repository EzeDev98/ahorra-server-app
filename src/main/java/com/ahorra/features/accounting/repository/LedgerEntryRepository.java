package com.ahorra.features.accounting.repository;

import com.ahorra.features.accounting.enums.AccountType;
import com.ahorra.features.accounting.enums.EntryDirection;
import com.ahorra.features.accounting.model.LedgerEntry;
import com.ahorra.features.day.dto.request.DaySpend;
import com.ahorra.features.income.dto.request.CategorySpendRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    Optional<LedgerEntry> findByIdAndUserId(Long id, Long userId);
    List<LedgerEntry> findAllByUserIdAndEntryDate(Long userId, LocalDate entryDate);
    long countByUserIdAndEntryDate(Long userId, LocalDate entryDate);

    @Query("""
            select new com.ahorra.feature.day.dto.DaySpend(e.entryDate, sum(t.amount))
            from TransactionEntry t join t.ledgerEntry e
            where e.userId = :userId
              and e.entryDate between :from and :to
              and t.direction = :direction
              and t.accountId in (select a.id from Account a where a.userId = :userId and a.type = :accountType)
            group by e.entryDate
            """)
    List<DaySpend> sumSpendByDay(@Param("userId") Long userId,
                                 @Param("accountType") AccountType accountType,
                                 @Param("direction") EntryDirection direction,
                                 @Param("from") LocalDate from,
                                 @Param("to") LocalDate to);

    @Query("""
            select new com.ahorra.feature.income.dto.CategorySpendRow(e.categoryId, sum(t.amount))
            from TransactionEntry t join t.ledgerEntry e
            where e.userId = :userId
              and e.entryDate between :from and :to
              and t.direction = :direction
              and t.accountId in (select a.id from Account a where a.userId = :userId and a.type = :accountType)
            group by e.categoryId
            """)
    List<CategorySpendRow> sumSpendByCategory(@Param("userId") Long userId,
                                              @Param("accountType") AccountType accountType,
                                              @Param("direction") EntryDirection direction,
                                              @Param("from") LocalDate from,
                                              @Param("to") LocalDate to);
}
