package com.ahorra.features.capture.transcription;

public interface TranscriptionService {
    String transcribe(byte[] audio, String contentType);
}
