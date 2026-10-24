package com.ahorra.common.money;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.ahorra.common.money.MoneyConfig.*;

@Component
public class MoneyUtils {
    public static BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return ZERO;
        }
        return amount.setScale(MONETARY_SCALE, MONETARY_ROUNDING);
    }

    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(ZERO) > 0;
    }

    public static boolean isLessThan(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).compareTo(normalize(amount2)) < 0;
    }

    public static boolean equals(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).compareTo(normalize(amount2)) == 0;
    }

    public static BigDecimal add(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).add(normalize(amount2));
    }
}
