package com.ahorra.features.capture.service;

import com.ahorra.features.capture.dto.request.ConfirmRequest;
import com.ahorra.features.capture.dto.response.PendingEntryResponse;

import java.util.List;

public interface PendingEntryService {
    List<PendingEntryResponse> listPending(Long userId);
    PendingEntryResponse confirm(Long userId, Long pendingEntryId, ConfirmRequest request);
    PendingEntryResponse discard(Long userId, Long pendingEntryId);
    PendingEntryResponse restore(Long userId, Long pendingEntryId);
}
