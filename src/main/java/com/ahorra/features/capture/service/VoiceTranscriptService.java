package com.ahorra.features.capture.service;

import com.ahorra.features.capture.dto.response.TranscriptResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VoiceTranscriptService {
    TranscriptResponse transcribe(Long userId, MultipartFile audio);
}
