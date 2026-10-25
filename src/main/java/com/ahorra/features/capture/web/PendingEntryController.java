package com.ahorra.features.capture.web;

import com.ahorra.features.capture.dto.response.PendingEntryResponse;
import com.ahorra.features.capture.service.PendingEntryService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("${app.title}")
@Tag(name = "Pending Entries API", description = "Resource for reviewing and confirming parsed entries")
public class PendingEntryController {

    private final PendingEntryService pendingEntryService;
    private final CurrentUserService currentUserService;

    @GetMapping("/entries/pending")
    public ResponseEntity<List<PendingEntryResponse>> listPending() {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(pendingEntryService.listPending(userId));
    }
}
