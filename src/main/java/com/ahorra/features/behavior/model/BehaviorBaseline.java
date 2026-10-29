package com.ahorra.features.behavior.model;

import com.ahorra.features.behavior.enums.DayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.ahorra.common.money.MoneyConfig.MONETARY_PRECISION;
import static com.ahorra.common.money.MoneyConfig.MONETARY_SCALE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "behavior_baselines")
public class BehaviorBaseline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "window_days", nullable = false)
    private int windowDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 10)
    private DayType dayType;

    @Column(name = "mean_amount", nullable = false, precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal meanAmount;

    @Column(name = "stv_dev", nullable = false, precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal stdDev;

    @Column(name = "sample_days", nullable = false)
    private int sampleDays;

    @Column(name = "confidence", nullable = false, precision = 4, scale = 3)
    private BigDecimal confidence;

    @Column(name = "computed_through", nullable = false)
    private LocalDate computedThrough;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
