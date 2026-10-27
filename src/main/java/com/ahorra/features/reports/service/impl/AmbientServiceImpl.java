package com.ahorra.features.reports.service.impl;

import com.ahorra.features.accounting.enums.AccountType;
import com.ahorra.features.accounting.enums.EntryDirection;
import com.ahorra.features.accounting.repository.LedgerEntryRepository;
import com.ahorra.features.capture.enums.FlowDirection;
import com.ahorra.features.capture.enums.PendingStatus;
import com.ahorra.features.capture.repository.PendingEntryRepository;
import com.ahorra.features.day.dto.request.DaySpend;
import com.ahorra.features.day.service.DayGateService;
import com.ahorra.features.income.dto.response.VarianceResponse;
import com.ahorra.features.income.service.VarianceService;
import com.ahorra.features.reports.dto.AmbientResponse;
import com.ahorra.features.reports.enums.ReportVerdict;
import com.ahorra.features.reports.service.AmbientService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class AmbientServiceImpl implements AmbientService {


    private final DayGateService dayGateService;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PendingEntryRepository pendingEntryRepository;
    private final AmbientAggregateCache aggregateCache;
    private final VarianceService varianceService;
    private final VerdictCalculator verdictCalculator;
    private final ZoneId zone;

    public AmbientServiceImpl(DayGateService dayGateService,
                              LedgerEntryRepository ledgerEntryRepository,
                              PendingEntryRepository pendingEntryRepository,
                              AmbientAggregateCache aggregateCache,
                              VarianceService varianceService,
                              VerdictCalculator verdictCalculator,
                              @Value("${app.timezone:Africa/Lagos}") String timezone) {
        this.dayGateService = dayGateService;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.pendingEntryRepository = pendingEntryRepository;
        this.aggregateCache = aggregateCache;
        this.varianceService = varianceService;
        this.verdictCalculator = verdictCalculator;
        this.zone = ZoneId.of(timezone);
    }

    @Override
    @Transactional(readOnly = true)
    public AmbientResponse today(Long userId) {
        dayGateService.assertNotGated(userId);
        LocalDate today = LocalDate.now(zone);

        BigDecimal confirmed = ledgerEntryRepository
                .sumSpendByDay(userId, AccountType.EXPENSE, EntryDirection.DEBIT, today, today)
                .stream()
                .map(DaySpend::spend)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pending = pendingEntryRepository.sumTotalByStatusAndFlowAndDay(userId, PendingStatus.PENDING, FlowDirection.EXPENSE, today);
        if (pending == null) {
            pending = BigDecimal.ZERO;
        }
        long pendingCount = pendingEntryRepository.countByUserIdAndStatusAndEntryDate(userId, PendingStatus.PENDING, today);

        BigDecimal provisional = confirmed.add(pending);

        AmbientAggregateCache.UsualSpend usual = aggregateCache.usualSpend(userId, today);

        ReportVerdict verdict;
        BigDecimal periodVariance = null;
        BigDecimal periodBaseline = null;
        try {
            VarianceResponse variance = varianceService.variance(userId, today);
            verdict = verdictCalculator.verdict(variance);
            periodVariance = variance.variance();
            periodBaseline = variance.baseline();
        } catch (EntityNotFoundException e) {
            verdict = ReportVerdict.NO_INCOME_SET;
        }

        return new AmbientResponse(today, confirmed, pending, (int) pendingCount, provisional, pendingCount > 0, usual.usualDailySpend(), usual.daysObserved(), usual.comparisonAvailable(), verdict, periodVariance, periodBaseline);
    }
}
