package com.ahorra.features.day.web;

import com.ahorra.features.day.dto.response.DayStatusResponse;
import com.ahorra.features.day.service.DayGateService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("${app.title}")
@Tag(name = "DAY API", description = "Resource for the day-close gate and forced resolution")
public class DayController {

    private final DayGateService dayGateService;
    private final CurrentUserService currentUserService;

    @GetMapping("/day/status")
    public ResponseEntity<DayStatusResponse> status() {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(dayGateService.status(userId));
    }
}
