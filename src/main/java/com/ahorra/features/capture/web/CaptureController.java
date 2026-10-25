package com.ahorra.features.capture.web;

import com.ahorra.features.capture.dto.request.CaptureRequest;
import com.ahorra.features.capture.dto.response.PendingEntryResponse;
import com.ahorra.features.capture.service.CaptureService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("${app.title}")
@RestController
@Tag(name = "Capture Api", description = "Resource for submitting spoken or typed entries for parsing")
public class CaptureController {

    private final CaptureService captureService;
    private final CurrentUserService currentUserService;

    @PostMapping("/entries/capture")
    public ResponseEntity<List<PendingEntryResponse>> capture(@Valid @RequestBody CaptureRequest request) {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(captureService.capture(userId, request));
    }
}
