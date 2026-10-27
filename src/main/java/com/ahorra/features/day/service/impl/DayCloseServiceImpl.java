package com.ahorra.features.day.service.impl;

import com.ahorra.common.exception.UserNotFoundException;
import com.ahorra.features.capture.enums.PendingStatus;
import com.ahorra.features.capture.repository.PendingEntryRepository;
import com.ahorra.features.day.enums.DayStatus;
import com.ahorra.features.day.event.DayClosedEvent;
import com.ahorra.features.day.model.DayState;
import com.ahorra.features.day.repository.DayStateRepository;
import com.ahorra.features.day.service.DayCloseService;
import com.ahorra.features.user.model.User;
import com.ahorra.features.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class DayCloseServiceImpl implements DayCloseService {

    private final DayStateRepository dayStateRepository;
    private final PendingEntryRepository pendingEntryRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ZoneId zone;

    public DayCloseServiceImpl(DayStateRepository dayStateRepository,
                               PendingEntryRepository pendingEntryRepository,
                               UserRepository userRepository, ApplicationEventPublisher eventPublisher,
                               @Value("${app.timezone:Africa/Lagos}") String timeZone) {
        this.dayStateRepository = dayStateRepository;
        this.pendingEntryRepository = pendingEntryRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.zone = ZoneId.of(timeZone);
    }


    @Override
    @Transactional
    public void catchup(Long userId, LocalDate through) {
        Set<LocalDate> pendingDays = new HashSet<>(pendingEntryRepository.findEntryDatesByStatus(userId, PendingStatus.PENDING));
        for (DayState suspended : dayStateRepository.findAllByUserIdAndStatus(userId, DayStatus.SUSPENDED)) {
            if (!pendingDays.contains(suspended.getDayDate())) {
                suspended.setStatus(DayStatus.CLOSED);
                suspended.setClosedAt(LocalDateTime.now());
                dayStateRepository.save(suspended);
                eventPublisher.publishEvent(new DayClosedEvent(userId, suspended.getDayDate(), true));
                log.info("Day {} closed retroactively for user {}", suspended.getDayDate(), userId);
            }
        }

        LocalDate next = dayStateRepository.findTopByUserIdOrderByDayDateDesc(userId)
                .map(dayState -> dayState.getDayDate().plusDays(1))
                .orElseGet(() -> firstDay(userId));

        for (LocalDate day = next; !day.isAfter(through); day = day.plusDays(1)) {
            DayState dayState = new DayState();
            dayState.setUserId(userId);
            dayState.setDayDate(day);
            if (pendingDays.contains(day)) {
                dayState.setStatus(DayStatus.SUSPENDED);
                dayState.setSuspendedAt(LocalDateTime.now());
                dayStateRepository.save(dayState);
                log.info("Day {} suspended for user {}: unconfirmed entries remain", day, userId);
            } else {
                dayState.setStatus(DayStatus.CLOSED);
                dayState.setClosedAt(LocalDateTime.now());
                dayStateRepository.save(dayState);
                eventPublisher.publishEvent(new DayClosedEvent(userId, day, false));
            }
        }
    }

    @Override
    public boolean isClosed(Long userId, LocalDate day) {
        return dayStateRepository.existsByUserIdAndDayDateAndStatus(userId, day, DayStatus.CLOSED);
    }

    private LocalDate firstDay(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return user.getCreatedAt() == null ? LocalDate.now(zone) : user.getCreatedAt().toLocalDate();
    }

}
