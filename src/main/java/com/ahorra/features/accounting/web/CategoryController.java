package com.ahorra.features.accounting.web;

import com.ahorra.features.accounting.dto.LookupItem;
import com.ahorra.features.accounting.service.impl.LookupCacheService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${app.title}")
public class CategoryController {

    private final LookupCacheService lookupCacheService;
    private final CurrentUserService currentUserService;

    @GetMapping("/categories")
    public ResponseEntity<List<LookupItem>> list() {
        Long userId = currentUserService.getCurrentUser().getId();
        List<LookupItem> responses = lookupCacheService.categories(userId).stream()
                .sorted(Comparator.comparing(LookupItem::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return ResponseEntity.ok(responses);
    }
}
