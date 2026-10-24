package com.ahorra.features.income.service.impl;

import com.ahorra.common.exception.BadRequestException;
import com.ahorra.common.exception.UserNotFoundException;
import com.ahorra.common.money.MoneyUtils;
import com.ahorra.features.accounting.enums.PeriodType;
import com.ahorra.features.accounting.model.IncomeHistory;
import com.ahorra.features.accounting.repository.IncomeHistoryRepository;
import com.ahorra.features.income.dto.request.IncomeRequest;
import com.ahorra.features.income.dto.response.IncomeResponse;
import com.ahorra.features.income.service.IncomeService;
import com.ahorra.features.user.model.User;
import com.ahorra.features.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.ahorra.common.constants.GeneralConstants.MAX_AMOUNT;
import static com.ahorra.common.constants.GeneralConstants.MAX_CUSTOM_PERIOD_DAYS;
import static com.ahorra.common.money.MoneyConfig.MONETARY_SCALE;

@Slf4j
@Service
public class IncomeServiceImpl implements IncomeService {

    private final IncomeHistoryRepository incomeHistoryRepository;
    private final UserRepository userRepository;
    private final ZoneId zone;

    public IncomeServiceImpl(IncomeHistoryRepository incomeHistoryRepository, UserRepository userRepository, @Value("${app.timezone:Africa/Lagos}")ZoneId zone) {
        this.incomeHistoryRepository = incomeHistoryRepository;
        this.userRepository = userRepository;
        this.zone = zone;
    }

    @Override
    public IncomeResponse getCurrentIncome(Long userId) {
        return incomeHistoryRepository.findByUserIdAndEffectiveToIsNull(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("No income has been set yet"));
    }

    @Override
    public List<IncomeResponse> getHistory(Long userId) {
        return incomeHistoryRepository.findAllByUserIdOrderByEffectiveFromDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public IncomeResponse setIncome(Long userId, IncomeRequest request) {
        IncomeHistory candidate = validateRequest(request);
        LocalDate today = LocalDate.now(zone);
        Optional<IncomeHistory> current = incomeHistoryRepository.findByUserIdAndEffectiveToIsNull(userId);

        if (current.isPresent() && createdToday(current.get(), today)) {
            IncomeHistory record = current.get();
            record.setMinAmount(candidate.getMinAmount());
            record.setMinAmount(candidate.getMaxAmount());
            record.setPeriodType(candidate.getPeriodType());
            record.setPeriodLength(candidate.getPeriodLength());
            return toResponse(incomeHistoryRepository.save(record));
        }

        LocalDate from = today;
        if (current.isPresent()) {
            IncomeHistory oldHistory = current.get();
            oldHistory.setEffectiveTo(today);
            incomeHistoryRepository.saveAndFlush(oldHistory);
        } else {
            from = firstEffectiveDate(userId, today);
        }

        candidate.setUserId(userId);
        candidate.setEffectiveFrom(from);
        IncomeHistory saved = incomeHistoryRepository.save(candidate);
        log.info("Income set for user {} effective fom {}", userId, from);
        return toResponse(saved);
    }

    private LocalDate firstEffectiveDate(Long userId, LocalDate today) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user.getCreatedAt() == null) {
            return today;
        }

        LocalDate registered = toZoneDate(user.getCreatedAt());
        return registered.isAfter(today) ? today : registered;
    }

    private boolean createdToday(IncomeHistory record, LocalDate today) {
        return record.getCreatedAt() != null && toZoneDate(record.getCreatedAt()).equals(today);
    }

    private LocalDate toZoneDate(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).withZoneSameInstant(zone).toLocalDate();
    }


    private IncomeHistory validateRequest(IncomeRequest request) {
        if (request == null || request.minAmount() == null || request.maxAmount() == null || request.periodType() == null) {
            throw new BadRequestException("Minimum, maximum and period type are required");
        }

        for (BigDecimal amount : List.of(request.minAmount(), request.maxAmount())) {
            if (amount.stripTrailingZeros().scale() > MONETARY_SCALE) {
                throw new BadRequestException("Amounts must have at most " + MONETARY_SCALE + " decimal places");
            }
        }

        BigDecimal min = MoneyUtils.normalize(request.minAmount());
        BigDecimal max = MoneyUtils.normalize(request.maxAmount());
        if (!MoneyUtils.isPositive(min)) {
            throw new BadRequestException("Minimum income must be greater than zero");
        }
        if (MoneyUtils.isLessThan(max, min)) {
            throw new BadRequestException("Maximum income cannot be less than the minimum");
        }
        if (max.compareTo(MAX_AMOUNT) >= 0) {
            throw new BadRequestException("Income amount is too large");
        }

        Integer periodLength = null;
        if (request.periodType() == PeriodType.CUSTOM) {
            periodLength = request.periodLength();
            if (periodLength == null || periodLength < 1 || periodLength > MAX_CUSTOM_PERIOD_DAYS) {
                throw new BadRequestException("A custom period needs a length between 1 and " + MAX_CUSTOM_PERIOD_DAYS + " days");
            }
        }

        IncomeHistory record = new IncomeHistory();
        record.setMinAmount(min);
        record.setMaxAmount(max);
        record.setPeriodType(request.periodType());
        record.setPeriodLength(periodLength);
        return record;
    }


    private IncomeResponse toResponse(IncomeHistory incomeHistory) {
        return new IncomeResponse(incomeHistory.getId(),
                incomeHistory.getMinAmount(),
                incomeHistory.getMaxAmount(),
                incomeHistory.getPeriodType(),
                incomeHistory.getPeriodLength(),
                incomeHistory.getEffectiveFrom(),
                incomeHistory.getEffectiveTo());
    }
}
