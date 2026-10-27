package com.ahorra.common.money;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static com.ahorra.common.money.MoneyConfig.MONETARY_ROUNDING;
import static com.ahorra.common.money.MoneyConfig.MONETARY_SCALE;
import static java.math.BigDecimal.ZERO;

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

    public static boolean isNegative(BigDecimal amount) {
        return amount != null && amount.compareTo(ZERO) < 0;
    }

    public static boolean isZero(BigDecimal amount) {
        return amount == null || amount.compareTo(ZERO) == 0;
    }

    public static boolean isGreaterThan(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).compareTo(normalize(amount2)) > 0;
    }

    public static boolean isGreaterThanOrEqual(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).compareTo(normalize(amount2)) >= 0;
    }

    public static boolean isLessThan(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).compareTo(normalize(amount2)) < 0;
    }

    public static boolean isLessThanOrEqual(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).compareTo(normalize(amount2)) <= 0;
    }

    public static boolean equals(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).compareTo(normalize(amount2)) == 0;
    }

    public static BigDecimal add(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).add(normalize(amount2));
    }

    public static BigDecimal subtract(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).subtract(normalize(amount2));
    }

    public static BigDecimal multiply(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).multiply(normalize(amount2)).setScale(MONETARY_SCALE, MONETARY_ROUNDING);
    }

    public static BigDecimal divide(BigDecimal amount1, BigDecimal amount2) {
        if (isZero(amount2)) {
            throw new ArithmeticException("Division by zero");
        }
        return amount1.divide(amount2, MONETARY_SCALE, MONETARY_ROUNDING);
    }

    public static BigDecimal percentage(BigDecimal amount, BigDecimal percentage) {
        return multiply(amount, divide(percentage, new BigDecimal("100")));
    }

    public static BigDecimal min(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).min(normalize(amount2));
    }

    public static BigDecimal max(BigDecimal amount1, BigDecimal amount2) {
        return normalize(amount1).max(normalize(amount2));
    }

    public static BigDecimal abs(BigDecimal amount) {
        return normalize(amount).abs();
    }

    public static String formatCurrency(BigDecimal amount, String currencySymbol) {
        if (amount == null) {
            return currencySymbol + "0.00";
        }
        DecimalFormat formatter = new DecimalFormat(currencySymbol + "#,##0.00");
        return formatter.format(normalize(amount));
    }

    public static boolean hasSufficientBalance(BigDecimal balance, BigDecimal amount) {
        return isGreaterThanOrEqual(balance, amount);
    }
}
