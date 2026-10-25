package com.ahorra.features.capture.model;

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
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pending_entry_items")
public class PendingEntryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pending_entry_id", nullable = false)
    private PendingEntry pendingEntry;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "amount", nullable = false, precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal amount;
}
