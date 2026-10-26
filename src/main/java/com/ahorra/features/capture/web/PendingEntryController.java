package com.ahorra.features.capture.web;

import com.ahorra.features.capture.dto.request.ConfirmRequest;
import com.ahorra.features.capture.dto.response.PendingEntryResponse;
import com.ahorra.features.capture.service.PendingEntryService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/entries/pending/{id}/confirm")
    public ResponseEntity<PendingEntryResponse> confirm(@PathVariable Long id, @Valid @RequestBody(required = false)ConfirmRequest request) {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(pendingEntryService.confirm(userId,id, request));
    }

    @PostMapping("/entries/pending/{id}/discard")
    public ResponseEntity<PendingEntryResponse> discard(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(pendingEntryService.discard(userId, id));
    }

    @PostMapping("/entries/pending/{id}/restore")
    public ResponseEntity<PendingEntryResponse> restore(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(pendingEntryService.restore(userId, id));
    }
}
