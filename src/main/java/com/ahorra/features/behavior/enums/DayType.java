package com.ahorra.features.behavior.enums;

import java.time.DayOfWeek;
import java.time.LocalDate;

public enum DayType {
    ALL,
    WEEKDAY,
    WEEKEND;

    public boolean matches(LocalDate day) {
        DayOfWeek dayOfWeek = day.getDayOfWeek();
        boolean weekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        return switch (this) {
            case ALL -> true;
            case WEEKEND -> weekend;
            case WEEKDAY -> !weekend;
        };
    }
}
