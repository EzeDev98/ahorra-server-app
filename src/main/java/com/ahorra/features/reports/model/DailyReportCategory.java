package com.ahorra.features.reports.model;

import com.ahorra.features.reports.enums.CategoryScope;
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
@Table(name = "daily_report_categories")
public class DailyReportCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 10)
    private CategoryScope scope;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "amount", nullable = false, precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal amount;

    @Column(name = "share_percent", nullable = false, precision = 6, scale = 2)
    private BigDecimal sharePercent;

    @Column(name = "attribute_excess", precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal attributedExcess;

}
