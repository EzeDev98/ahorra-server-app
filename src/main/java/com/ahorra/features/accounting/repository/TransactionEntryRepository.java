package com.ahorra.features.accounting.repository;

import com.ahorra.features.accounting.model.TransactionEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, Long> {

}
