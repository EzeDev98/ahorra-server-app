package com.ahorra.features.reports.service.impl;

import com.ahorra.features.reports.dto.ReportFacts;

import java.math.BigDecimal;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NarrativeGuard {

    static final int MAX_LENGTH = 900;
    private static final Pattern NUMBER = Pattern.compile("\\d[\\d,]*(?:\\.\\d+)?");

    private NarrativeGuard() {
    }

    public static boolean isAcceptable(String text, ReportFacts facts) {

        if (text == null || text.isBlank() || text.length() > MAX_LENGTH) {
            return false;
        }

        Set<String> allowed = facts.allowedNumbers();
        Matcher matcher = NUMBER.matcher(text);
        while (matcher.find()) {
            String token = matcher.group().replace(",", "");
            try {
                String normalised = new BigDecimal(token).stripTrailingZeros().toPlainString();
                if (!allowed.contains(normalised)) {
                    return false;
                }
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        return true;
    }
}
