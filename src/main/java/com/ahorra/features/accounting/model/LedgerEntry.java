package com.ahorra.features.accounting.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @OneToMany(mappedBy = "ledgerEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEntry> lines = new ArrayList<>();

    public void addLine(TransactionEntry line) {
        line.setLedgerEntry(this);
        lines.add(line);
    }
}
