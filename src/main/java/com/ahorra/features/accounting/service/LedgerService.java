package com.ahorra.features.accounting.service;

import com.ahorra.features.accounting.dto.PostEntryRequest;
import com.ahorra.features.accounting.model.LedgerEntry;

public interface LedgerService {
    LedgerEntry postEntry(PostEntryRequest request);
}
