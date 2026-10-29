package com.ahorra.features.behavior.model;

import com.ahorra.features.behavior.enums.HabitStatus;
import com.ahorra.features.behavior.enums.TrendDirection;
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
@Table(name = "detected_habits")
public class DetectedHabit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "first_observed", nullable = false)
    private LocalDate firstObserved;

    @Column(name = "last_observed", nullable = false)
    private LocalDate lastObserved;

    @Column(name = "frequency_per_week", nullable = false, precision = 5, scale = 2)
    private BigDecimal frequencyPerWeek;

    @Column(name = "avg_amount", nullable = false, precision = MONETARY_PRECISION, scale = MONETARY_SCALE)
    private BigDecimal avgAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "trend_direction", nullable = false, length = 10)
    private TrendDirection trendDirection;

    @Column(name = "confidence", nullable = false, precision = 4, scale = 3)
    private BigDecimal confidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private HabitStatus status;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
