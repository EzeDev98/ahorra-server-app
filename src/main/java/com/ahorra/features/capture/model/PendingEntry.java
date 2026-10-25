package com.ahorra.features.capture.model;

import com.ahorra.features.capture.enums.FlowDirection;
import com.ahorra.features.capture.enums.PendingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.ahorra.common.money.MoneyConfig.MONETARY_PRECISION;
import static com.ahorra.common.money.MoneyConfig.MONETARY_SCALE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pending_entries")
public class PendingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "raw_entry_id", nullable = false)
    private Long rawEntryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "flow", nullable = false, length = 10)
    private FlowDirection flow;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "vendor")
    private String vendor;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "total_amount", nullable = false, precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PendingStatus status = PendingStatus.PENDING;

    @Column(name = "ledger_entry_id")
    private Long ledgerEntryId;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "discarded_at")
    private LocalDateTime discardedAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "pendingEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<PendingEntryItem> items = new ArrayList<>();

    public void markPosted(long ledgerEntryId, boolean edited) {
        if (status !=PendingStatus.PENDING) {
            throw new IllegalStateException("Only a PENDING entry can be confirmed");
        }

        this.status = edited ? PendingStatus.EDITED : PendingStatus.CONFIRMED;
        this.ledgerEntryId = ledgerEntryId;
        this.confirmedAt = LocalDateTime.now();
    }

    public void markDiscarded() {
        if (status != PendingStatus.PENDING) {
            throw new IllegalStateException("Only a PENDING entry can be discarded");
        }

        this.status = PendingStatus.DISCARDED;
        this.discardedAt = LocalDateTime.now();
    }

    public void restore(LocalDate date) {
        if (status != PendingStatus.DISCARDED) {
            throw new IllegalStateException("Only a discarded entry can be restored");
        }
        this.status = PendingStatus.PENDING;
        this.discardedAt = null;
        this.entryDate = date;
    }

    public void addItem(PendingEntryItem item) {
        item.setPendingEntry(this);
        item.setPosition(items.size());
        items.add(item);
    }
}
