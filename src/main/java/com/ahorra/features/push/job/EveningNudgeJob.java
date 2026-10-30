package com.ahorra.features.push.job;

import com.ahorra.features.capture.enums.PendingStatus;
import com.ahorra.features.capture.repository.PendingEntryRepository;
import com.ahorra.features.push.service.impl.PushSender;
import com.ahorra.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EveningNudgeJob {

    static final String TITLE = "Ahorra";
    static final String BODY = "You have entries waiting to be confirmed before the day closes.";

    private final UserRepository userRepository;
    private final PendingEntryRepository pendingEntryRepository;
    private final PushSender pushSender;

    @Scheduled(cron = "${app.push.nudge-cron:0 0 21 * * *}", zone = "${app.timezone:Africa/Lagos}")
    public void sendNudges() {
        if (!pushSender.isEnabled()) {
            return;
        }
        int notified = 0;
        for (Long userId : userRepository.findReminderEnabledUserIds()) {
            try {
                if (pendingEntryRepository.findEntryDatesByStatus(userId, PendingStatus.PENDING).isEmpty()) {
                    continue;
                }
                if (pushSender.send(userId, TITLE, BODY) > 0) {
                    notified++;
                }
            } catch (RuntimeException ex) {
                log.error("Evening nudge failed for user {}", userId, ex);
            }
        }
        log.info("Evening nudge sent to {} users", notified);
    }
}

