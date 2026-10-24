package com.ahorra.features.income.web;

import com.ahorra.features.income.dto.request.IncomeRequest;
import com.ahorra.features.income.dto.response.IncomeResponse;
import com.ahorra.features.income.service.IncomeService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${app.title}")
public class IncomeController {

    private final IncomeService incomeService;
    private final CurrentUserService currentUserService;

    @GetMapping("/income")
    public ResponseEntity<IncomeResponse> getCurrentIncome() {
        Long userId = currentUserService.getCurrentUser().getId();
        IncomeResponse response = incomeService.getCurrentIncome(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/income")
    public ResponseEntity<IncomeResponse> setIncome(@Valid @RequestBody IncomeRequest request) {
        Long userId = currentUserService.getCurrentUser().getId();
        IncomeResponse response = incomeService.setIncome(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/income/history")
    public ResponseEntity<List<IncomeResponse>> getHistory() {
        Long userId = currentUserService.getCurrentUser().getId();
        List<IncomeResponse> responses = incomeService.getHistory(userId);
        return ResponseEntity.ok(responses);
    }

}
