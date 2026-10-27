package com.ahorra.features.reports.web;

import com.ahorra.features.reports.dto.DailyReportResponse;
import com.ahorra.features.reports.dto.DailyReportSummary;
import com.ahorra.features.reports.service.ReportQueryService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${app.title}")
@RequiredArgsConstructor
public class ReportController {

    private final ReportQueryService reportQueryService;
    private final CurrentUserService currentUserService;

    @GetMapping("/report/daily")
    public ResponseEntity<List<DailyReportSummary>> history(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(reportQueryService.history(userId, from, to));
    }

    @GetMapping("/report/daily/latest")
    public ResponseEntity<DailyReportResponse> latest() {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(reportQueryService.latest(userId));
    }

    @GetMapping("/report/daily/{date}")
    public ResponseEntity<DailyReportResponse> forDay(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(reportQueryService.forDay(userId, date));
    }
}
