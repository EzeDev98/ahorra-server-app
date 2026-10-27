package com.ahorra.features.reports.service.impl;

import com.ahorra.common.money.MoneyUtils;
import com.ahorra.features.income.dto.response.VarianceResponse;
import com.ahorra.features.reports.enums.ReportVerdict;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class VerdictCalculator {

    static final BigDecimal UPPER = new BigDecimal("1.10");
    static final BigDecimal LOWER = new BigDecimal("0.90");

    public ReportVerdict verdict(VarianceResponse varianceResponse) {

        if (MoneyUtils.isPositive(varianceResponse.variance())) {
            return ReportVerdict.OVER;
        }

        BigDecimal expectedSoFar = MoneyUtils.divide(MoneyUtils.multiply(varianceResponse.baseline(), BigDecimal.valueOf(varianceResponse.daysElapsed())),
                BigDecimal.valueOf(varianceResponse.daysInPeriod()));

        if (MoneyUtils.isGreaterThan(varianceResponse.spend(), MoneyUtils.multiply(expectedSoFar, UPPER))) {
            return ReportVerdict.OVER;
        }

        if (MoneyUtils.isLessThan(varianceResponse.spend(), MoneyUtils.multiply(expectedSoFar, LOWER))) {
            return ReportVerdict.UNDER;
        }

        return ReportVerdict.ON_TRACK;
    }
}
