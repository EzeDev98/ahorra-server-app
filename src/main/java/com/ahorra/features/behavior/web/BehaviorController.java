package com.ahorra.features.behavior.web;

import com.ahorra.features.behavior.dto.BehaviorOverview;
import com.ahorra.features.behavior.service.BehaviorOverviewService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.title}")
@RequiredArgsConstructor
public class BehaviorController {

    private final BehaviorOverviewService behaviorOverviewService;
    private final CurrentUserService currentUserService;

    @GetMapping("/behavior/overview")
    public ResponseEntity<BehaviorOverview> overview() {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(behaviorOverviewService.overview(userId));
    }
}
