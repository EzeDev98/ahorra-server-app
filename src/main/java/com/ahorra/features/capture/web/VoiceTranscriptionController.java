package com.ahorra.features.capture.web;

import com.ahorra.features.capture.dto.response.TranscriptResponse;
import com.ahorra.features.capture.service.VoiceTranscriptService;
import com.ahorra.features.user.service.impl.CurrentUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@RestController
@RequestMapping("${app.title}")
@Tag(name  = "Voice Transcript API", description = "Resource for turning a short recording into a text for review")
public class VoiceTranscriptionController {

    private final VoiceTranscriptService voiceTranscriptService;
    private final CurrentUserService currentUserService;

    @PostMapping(value = "/entries/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TranscriptResponse> transcribe(@RequestPart("audio")MultipartFile audio) {
        Long userId = currentUserService.getCurrentUser().getId();
        return ResponseEntity.ok(voiceTranscriptService.transcribe(userId, audio));
    }
}
