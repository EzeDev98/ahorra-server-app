package com.ahorra.features.capture.service;

import com.ahorra.features.capture.dto.response.PendingEntryResponse;

import java.util.List;

public interface PendingEntryService {
    List<PendingEntryResponse> listPending(Long userId);
}
