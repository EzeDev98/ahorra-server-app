package com.ahorra.features.accounting.model;

import com.ahorra.features.accounting.enums.EntryDirection;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import static com.ahorra.common.money.MoneyConfig.MONETARY_PRECISION;
import static com.ahorra.common.money.MoneyConfig.MONETARY_SCALE;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transaction_entries")
public class TransactionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ledger_entry_id", nullable = false)
    private LedgerEntry ledgerEntry;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private EntryDirection direction;

    @Column(name = "amount", nullable = false, precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal amount;
}