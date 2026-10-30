package com.ahorra.features.appearance.web;

import com.ahorra.features.appearance.dto.AppearanceSetting;
import com.ahorra.features.appearance.service.AppearanceService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("${app.title}")
@Tag(name = "Appearance API", description = "Resource for the light or dark theme the user chose")
public class AppearanceController {

    private final AppearanceService appearanceService;
    private final CurrentUserService currentUserService;

    @GetMapping("/appearance")
    public ResponseEntity<AppearanceSetting> getAppearance() {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(new AppearanceSetting(appearanceService.getTheme(userId)));
    }

    @PutMapping("/appearance")
    public ResponseEntity<AppearanceSetting> setAppearance(@Valid @RequestBody AppearanceSetting request) {
        Long userId = currentUserService.getCurrentUser().getId();
        appearanceService.setTheme(userId, request.theme());
        return ResponseEntity.ok(request);
    }
}
