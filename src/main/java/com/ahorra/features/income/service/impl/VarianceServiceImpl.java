package com.ahorra.features.income.service.impl;

import com.ahorra.common.exception.BadRequestException;
import com.ahorra.common.money.MoneyUtils;
import com.ahorra.features.accounting.enums.AccountType;
import com.ahorra.features.accounting.enums.EntryDirection;
import com.ahorra.features.accounting.model.Category;
import com.ahorra.features.accounting.model.IncomeHistory;
import com.ahorra.features.accounting.repository.CategoryRepository;
import com.ahorra.features.accounting.repository.IncomeHistoryRepository;
import com.ahorra.features.accounting.repository.LedgerEntryRepository;
import com.ahorra.features.income.dto.request.CategorySpendRow;
import com.ahorra.features.income.dto.response.CategoryBreakdown;
import com.ahorra.features.income.dto.response.VarianceResponse;
import com.ahorra.features.income.service.VarianceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static com.ahorra.common.constants.GeneralConstants.HUNDRED;
import static com.ahorra.common.constants.GeneralConstants.UNCATEGORIZED;

@Slf4j
@Service
public class VarianceServiceImpl implements VarianceService {

    private final IncomeHistoryRepository incomeHistoryRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final CategoryRepository categoryRepository;
    private final ZoneId zone;

    public VarianceServiceImpl(IncomeHistoryRepository incomeHistoryRepository,
                               LedgerEntryRepository ledgerEntryRepository,
                               CategoryRepository categoryRepository,
                               @Value("${app.timezone:Africa/Lagos}")
                               String timezone) {
        this.incomeHistoryRepository = incomeHistoryRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.categoryRepository = categoryRepository;
        this.zone = ZoneId.of(timezone);
    }

    @Override
    public VarianceResponse variance(Long userId, LocalDate asOf) {
        LocalDate today = LocalDate.now(zone);
        LocalDate day = asOf == null ? today : asOf;

        if (day.isAfter(today)) {
            throw new BadRequestException("Date cannot be in the future");
        }

        IncomeHistory income = incomeHistoryRepository.findActiveOn(userId, day)
                .orElseThrow(() -> new EntityNotFoundException("No income was set for that date"));

        Period period = periodFor(income, day);
        List<CategorySpendRow> rows = ledgerEntryRepository.sumSpendByCategory(userId, AccountType.EXPENSE, EntryDirection.DEBIT, period.start(), day);

        BigDecimal spend = rows.stream().map(CategorySpendRow::amount).reduce(BigDecimal.ZERO, MoneyUtils::add);
        BigDecimal baseline = income.getMinAmount();
        BigDecimal variance = MoneyUtils.subtract(spend, baseline);
        boolean over = MoneyUtils.isPositive(variance);

        Set<Long> categoryIds = rows.stream().map(CategorySpendRow::categoryId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> names = categoryIds.isEmpty() ? Map.of() : categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        List<CategoryBreakdown> categories = rows.stream()
                .sorted(Comparator.comparing(CategorySpendRow::amount).reversed())
                .map(row -> toBreakdown(row, names, spend, variance, over))
                .toList();

        return new VarianceResponse(day, income.getPeriodType(), period.start(), period.end(), (int) ChronoUnit.DAYS.between(period.start(), day) + 1, (int) ChronoUnit.DAYS.between(period.start(), period.end()) + 1, income.getMinAmount(), income.getMaxAmount(), baseline, MoneyUtils.normalize(spend), variance, categories);
    }

    private CategoryBreakdown toBreakdown(CategorySpendRow row, Map<Long, String> names, BigDecimal totalSpend, BigDecimal variance, boolean over) {
        BigDecimal amount = MoneyUtils.normalize(row.amount());
        String name = row.categoryId() == null ? UNCATEGORIZED : names.getOrDefault(row.categoryId(), UNCATEGORIZED);

        BigDecimal share = MoneyUtils.isPositive(totalSpend) ? MoneyUtils.divide(MoneyUtils.multiply(amount, HUNDRED), totalSpend) : MoneyUtils.normalize(BigDecimal.ZERO);
        BigDecimal attributeExcess = over && MoneyUtils.isPositive(totalSpend) ? MoneyUtils.divide(MoneyUtils.multiply(variance, amount), totalSpend) : MoneyUtils.normalize(BigDecimal.ZERO);
        return new CategoryBreakdown(row.categoryId(), name, amount, share, attributeExcess);
    }

    private Period periodFor(IncomeHistory income, LocalDate day) {
        return switch (income.getPeriodType()) {
            case DAILY -> new Period(day, day);
            case WEEKLY -> {
                LocalDate start = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new Period(start, start.plusDays(6));
            }
            case MONTHLY -> new Period(day.withDayOfMonth(1), day.with(TemporalAdjusters.lastDayOfMonth()));
            case CUSTOM -> {
                int length = income.getPeriodLength();
                long blocks = ChronoUnit.DAYS.between(income.getEffectiveFrom(), day) / length;
                LocalDate start = income.getEffectiveFrom().plusDays(blocks * length);
                yield new Period(start, start.plusDays(length - 1L));
            }
        };
    }


    private record Period(LocalDate start, LocalDate end) {

    }

}
