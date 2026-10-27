package com.ahorra.features.day.service.impl;

import com.ahorra.common.money.MoneyUtils;
import com.ahorra.features.accounting.enums.AccountType;
import com.ahorra.features.accounting.enums.EntryDirection;
import com.ahorra.features.accounting.repository.LedgerEntryRepository;
import com.ahorra.features.day.dto.request.DaySpend;
import com.ahorra.features.day.enums.DayStatus;
import com.ahorra.features.day.repository.DayStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DailySpendService {

    private final DayStateRepository dayStateRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public List<DaySpend> closedDaySpend(Long userId, LocalDate from, LocalDate to) {

        Map<LocalDate, BigDecimal> spendByDay = ledgerEntryRepository
                .sumSpendByDay(userId, AccountType.EXPENSE, EntryDirection.DEBIT, from, to).stream()
                .collect(Collectors.toMap(DaySpend::day, DaySpend::spend));

        return dayStateRepository
                .findAllByUserIdAndStatusAndDayDateBetweenOrderByDayDateAsc(userId,  DayStatus.CLOSED, from, to)
                .stream()
                .map(dayState -> new DaySpend(dayState.getDayDate(), MoneyUtils.normalize(spendByDay.getOrDefault(dayState.getDayDate(), BigDecimal.ZERO))))
                .toList();
    }
}
