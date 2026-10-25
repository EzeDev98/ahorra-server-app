package com.ahorra.features.capture.service.impl;

import com.ahorra.features.accounting.model.Account;
import com.ahorra.features.accounting.model.Category;
import com.ahorra.features.accounting.repository.AccountRepository;
import com.ahorra.features.accounting.repository.CategoryRepository;
import com.ahorra.features.capture.dto.request.ParsedItem;
import com.ahorra.features.capture.dto.response.PendingEntryResponse;
import com.ahorra.features.capture.model.PendingEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class PendingEntryMapper {

    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;


    public PendingEntryResponse toResponse(PendingEntry entry) {
        return toResponse(List.of(entry)).get(0);
    }

    public List<PendingEntryResponse> toResponse(List<PendingEntry> entries) {
        Set<Long> categoryIds = entries.stream().map(PendingEntry::getCategoryId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Set<Long> accountIds = entries.stream().map(PendingEntry::getAccountId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> categoryNames = categoryIds.isEmpty() ? Map.of() : categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        Map<Long, String> accountNames = accountIds.isEmpty() ? Map.of() : accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getId, Account::getName));

        return entries.stream().map(pendingEntry -> new PendingEntryResponse(
                pendingEntry.getId(), pendingEntry.getRawEntryId(), pendingEntry.getStatus(), pendingEntry.getFlow(), pendingEntry.getEntryDate(), pendingEntry.getVendor(),
                pendingEntry.getCategoryId(), pendingEntry.getCategoryId() == null ? null : categoryNames.get(pendingEntry.getCategoryId()),
                pendingEntry.getAccountId(), pendingEntry.getAccountId() == null ? null : accountNames.get(pendingEntry.getAccountId()),
                pendingEntry.getItems().stream().map(item -> new ParsedItem(item.getDescription(), item.getAmount())).toList(),
                pendingEntry.getTotalAmount())).toList();
    }
}
