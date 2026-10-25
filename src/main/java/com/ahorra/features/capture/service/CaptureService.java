package com.ahorra.features.capture.service;

import com.ahorra.features.capture.dto.request.CaptureRequest;
import com.ahorra.features.capture.dto.response.PendingEntryResponse;

import java.util.List;

public interface CaptureService {
    List<PendingEntryResponse> capture(Long userId, CaptureRequest request);
}
