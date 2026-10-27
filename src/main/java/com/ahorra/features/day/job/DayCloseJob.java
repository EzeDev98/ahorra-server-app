package com.ahorra.features.day.job;

import com.ahorra.features.day.service.DayCloseService;
import com.ahorra.features.reports.service.DailyReportService;
import com.ahorra.features.user.model.User;
import com.ahorra.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class DayCloseJob {

    private final UserRepository userRepository;
    private final DayCloseService dayCloseService;
    private final DailyReportService dailyReportService;

    @Value("${app.timezone:Africa/Lagos}")
    private String timezone;

    @Scheduled(cron = "${app.day.close-cron: 0 59 23 * * *}", zone = "${app.timezone:Africa/Lagos}")
    public void closeDay() {
        LocalDate today = LocalDate.now(ZoneId.of(timezone));
        log.info("Running day-close check for {}", today);
        for (User user : userRepository.findAll()) {
            try {
                dayCloseService.catchup(user.getId(), today);
                dailyReportService.generateMissing(user.getId());
            } catch (RuntimeException ex) {
                log.error("Day-close check failed for user {}", user.getId(), ex);
            }
        }
    }
}
