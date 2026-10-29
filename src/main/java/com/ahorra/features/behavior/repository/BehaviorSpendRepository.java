package com.ahorra.features.behavior.repository;

import com.ahorra.features.accounting.enums.AccountType;
import com.ahorra.features.accounting.enums.EntryDirection;
import com.ahorra.features.accounting.model.TransactionEntry;
import com.ahorra.features.behavior.dto.CategoryDaySpend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BehaviorSpendRepository extends JpaRepository<TransactionEntry, Long> {
    @Query("""
            select new com.ahorra.features.behavior.dto.CategoryDaySpend(e.entryDate, e.categoryId, sum(t.amount))
            from TransactionEntry t join t.ledgerEntry e
            where e.userId = :userId
              and e.entryDate between :from and :to
              and t.direction = :direction
              and t.accountId in (select a.id from Account a where a.userId = :userId and a.type = :accountType)
            group by e.entryDate, e.categoryId
            """)
    List<CategoryDaySpend> sumSpendByDayAndCategory(@Param("userId") Long userId, @Param("accountType") AccountType accountType, @Param("direction") EntryDirection direction, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
