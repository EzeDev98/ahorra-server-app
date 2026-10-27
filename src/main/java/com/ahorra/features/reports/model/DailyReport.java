package com.ahorra.features.reports.model;

import com.ahorra.features.accounting.enums.PeriodType;
import com.ahorra.features.reports.enums.NarrativeSource;
import com.ahorra.features.reports.enums.ReportKind;
import com.ahorra.features.reports.enums.ReportVerdict;
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
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "daily_reports")
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 20)
    private ReportKind kind;

    @Column(name = "summary_text", nullable = false, length = 1000)
    private String summaryText;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", length = 20)
    private PeriodType periodType;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "entry_count")
    private Integer entryCount;

    @Column(name = "day_spend", precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal daySpend;

    @Column(name = "baseline", precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal baseline;

    @Column(name = "income_max", precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal incomeMax;

    @Column(name = "period_spend", precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal periodSpend;

    @Column(name = "variance", precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal variance;

    @Column(name = "rolling_avg_daily_spend", precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal rollingAvgDailySpend;

    @Column(name = "days_observed")
    private Integer daysObserved;

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", length = 20)
    private ReportVerdict verdict;

    @Column(name = "insight_text", length = 500)
    private String insightText;

    @Enumerated(EnumType.STRING)
    @Column(name = "narrative_source", length = 20)
    private NarrativeSource narrativeSource;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("scope ASC, position ASC")
    private List<DailyReportCategory> categories = new ArrayList<>();

    public void addCategory(DailyReportCategory category) {
        category.setReport(this);
        categories.add(category);
    }
}


