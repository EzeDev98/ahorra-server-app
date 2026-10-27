package com.ahorra.features.day.service;

import java.time.LocalDate;

public interface DayCloseService {
    void catchup(Long userId, LocalDate through);
    boolean isClosed(Long userId, LocalDate day);
}
