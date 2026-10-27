package com.ahorra.features.day.service.impl;

import com.ahorra.common.exception.DayGatedException;
import com.ahorra.features.capture.enums.PendingStatus;
import com.ahorra.features.capture.model.PendingEntry;
import com.ahorra.features.capture.repository.PendingEntryRepository;
import com.ahorra.features.capture.service.impl.PendingEntryMapper;
import com.ahorra.features.day.dto.request.DayGate;
import com.ahorra.features.day.dto.response.DayStatusResponse;
import com.ahorra.features.day.enums.GateReason;
import com.ahorra.features.day.service.DayCloseService;
import com.ahorra.features.day.service.DayGateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
public class DayGateServiceImpl implements DayGateService {

    private final PendingEntryRepository pendingEntryRepository;
    private final PendingEntryMapper pendingEntryMapper;
    private final DayCloseService dayCloseService;
    private final ZoneId zone;
    private final LocalTime hardGateTime;

    public DayGateServiceImpl(PendingEntryRepository pendingEntryRepository,
                              PendingEntryMapper pendingEntryMapper,
                              DayCloseService dayCloseService,
                              @Value("${app.timezone:Africa/Lagos}")
                              String timeZone,
                              @Value("${app.day.hard-gate-time:22:00}")
                              String hardGateTime) {
        this.pendingEntryRepository = pendingEntryRepository;
        this.pendingEntryMapper = pendingEntryMapper;
        this.dayCloseService = dayCloseService;
        this.zone = ZoneId.of(timeZone);
        this.hardGateTime = LocalTime.parse(hardGateTime);
    }

    @Override
    public DayGate evaluate(Long userId) {

        LocalDate today = LocalDate.now(zone);
        LocalTime now = LocalTime.now(zone);

        List<LocalDate> days = pendingEntryRepository.findEntryDatesByStatus(userId, PendingStatus.PENDING)
                .stream().sorted().toList();

        List<LocalDate> pastDays = days.stream().filter(date -> date.isBefore(today)).toList();
        if (!pastDays.isEmpty()) {
            return new DayGate(true, GateReason.SUSPENDED_DAY, pastDays.get(0), pastDays.size() - 1);
        }

        if (days.contains(today) && !now.isBefore(hardGateTime)) {
            return new DayGate(true, GateReason.HARD_GATE_TODAY, today, 0);
        }

        return DayGate.open();
    }

    @Override
    public void assertNotGated(Long userId) {
        DayGate gate = evaluate(userId);
        if (!gate.gated()) {
            return;
        }

        String message = gate.reason() == GateReason.SUSPENDED_DAY ? "Please confirm your pending entries for " + gate.day() +
                " before continuing." : "It is past " + hardGateTime +
                ". Please confirm your pending entries before logging more.";
        throw new DayGatedException(message);
    }

    @Override
    public DayStatusResponse status(Long userId) {
        LocalDate today = LocalDate.now(zone);
        dayCloseService.catchup(userId, today.minusDays(1));
        DayGate gate = evaluate(userId);
        List<PendingEntry> shown = pendingEntryRepository.findAllByUserIdAndStatusOrderByEntryDateAscIdAsc(userId, PendingStatus.PENDING);
        return new DayStatusResponse(gate.gated(), gate.reason(), gate.day(), gate.otherDaysRemaining(), hardGateTime.toString(),pendingEntryMapper.toResponse(shown));
    }
}
