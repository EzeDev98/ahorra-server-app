package com.ahorra.features.reports.web;

import com.ahorra.features.reports.dto.AmbientResponse;
import com.ahorra.features.reports.service.AmbientService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.title}")
@RequiredArgsConstructor
public class AmbientController {

    private final AmbientService ambientService;
    private final CurrentUserService currentUserService;

    @GetMapping("/report/ambient")
    public ResponseEntity<AmbientResponse> ambient() {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(ambientService.today(userId));
    }
}
