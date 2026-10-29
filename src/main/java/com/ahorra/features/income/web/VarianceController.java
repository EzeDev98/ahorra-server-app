package com.ahorra.features.income.web;

import com.ahorra.features.income.dto.response.VarianceResponse;
import com.ahorra.features.income.service.VarianceService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@AllArgsConstructor
@RestController
@RequestMapping("${app.title}")
@Tag(name = "Variance Api", description = "Resource for spend versus income baseline")
public class VarianceController {

    private final VarianceService varianceService;
    private final CurrentUserService currentUserService;

    @GetMapping("/income/variance")
    public ResponseEntity<VarianceResponse> variance(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date) {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(varianceService.variance(userId, date));
    }
}
