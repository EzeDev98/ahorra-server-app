package com.ahorra.features.capture.service.impl;

import com.ahorra.features.capture.dto.response.PendingEntryResponse;
import com.ahorra.features.capture.enums.PendingStatus;
import com.ahorra.features.capture.repository.PendingEntryRepository;
import com.ahorra.features.capture.service.PendingEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PendingEntryServiceImpl implements PendingEntryService {

    private final PendingEntryRepository pendingEntryRepository;
    private final PendingEntryMapper pendingEntryMapper;

    @Override
    public List<PendingEntryResponse> listPending(Long userId) {
        return pendingEntryMapper.toResponse(pendingEntryRepository.findAllByUserIdAndStatusOrderByEntryDateAscIdAsc(userId, PendingStatus.PENDING));
    }
}
