package com.ahorra.features.capture.service.impl;

import com.ahorra.common.exception.BadRequestException;
import com.ahorra.features.capture.dto.response.TranscriptResponse;
import com.ahorra.features.capture.service.VoiceTranscriptService;
import com.ahorra.features.capture.transcription.TranscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

import static com.ahorra.common.constants.GeneralConstants.MAX_TEXT_LENGTH;
import static com.ahorra.common.constants.GeneralConstants.MAX_VOICE_BYTES;

@Slf4j
@RequiredArgsConstructor
@Service
public class VoiceTranscriptServiceImpl implements VoiceTranscriptService {

    private final TranscriptionService transcriptionService;

    @Override
    public TranscriptResponse transcribe(Long userId, MultipartFile audio) {

        if (audio == null || audio.isEmpty()) {
            throw new BadRequestException("The recording is empty. Please try again");
        }

        if (audio.getSize() > MAX_VOICE_BYTES)  {
            throw new BadRequestException("The recording is too long. Keep it under a minute");
        }

        String contentType = audio.getContentType() == null ? "" : audio.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("audio/") && !contentType.startsWith("video/webm") && !contentType.startsWith("video/mp4")) {
            throw new BadRequestException("That file is not a voice recording");
        }

        byte[] bytes;

        try {
            bytes = audio.getBytes();
        } catch (IOException ex) {
            throw new BadRequestException("The recording could not be read. Please try again");
        }

        String text = transcriptionService.transcribe(bytes, contentType);
        if (text.isBlank()) {
            throw new BadRequestException("We could not hear anything in that recording. Please try again.");
        }

        if (text.length() > MAX_TEXT_LENGTH) {
            throw new BadRequestException("That recording is too long. Keep it to one or two sentences");
        }

        return new TranscriptResponse(text);
    }
}
