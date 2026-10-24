package com.ahorra.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyConfig {
    public static final int MONETARY_PRECISION = 15;
    public static final int MONETARY_SCALE = 2;
    public static final RoundingMode MONETARY_ROUNDING = RoundingMode.HALF_UP;
    public static final BigDecimal ZERO = BigDecimal.ZERO;
}
