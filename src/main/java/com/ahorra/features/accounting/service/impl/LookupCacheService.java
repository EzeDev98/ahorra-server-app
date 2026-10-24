package com.ahorra.features.accounting.service.impl;

import com.ahorra.common.cache.RedisJsonCache;
import com.ahorra.features.accounting.dto.LookupItem;
import com.ahorra.features.accounting.enums.AccountType;
import com.ahorra.features.accounting.model.Account;
import com.ahorra.features.accounting.model.Category;
import com.ahorra.features.accounting.repository.AccountRepository;
import com.ahorra.features.accounting.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LookupCacheService {

    private static final Duration TTL = Duration.ofMinutes(5);

    public record LookupSnapshot(List<LookupItem> captureAccounts, List<LookupItem> categories) {
    }

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final DefaultCategoryService defaultCategories;
    private final RedisJsonCache cache;

    public List<LookupItem> captureAccounts(Long userId) {
        return snapshot(userId).captureAccounts();
    }

    public List<LookupItem> categories(Long userId) {
        return snapshot(userId).categories();
    }

    public void evict(Long userId) {
        cache.evict(key(userId));
    }

    private LookupSnapshot snapshot(Long userId) {
        String key = key(userId);
        Optional<LookupSnapshot> cached = cache.get(key, LookupSnapshot.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        LookupSnapshot fresh = load(userId);
        if (!fresh.captureAccounts().isEmpty()) {
            cache.put(key, fresh, TTL);
        }
        return fresh;
    }

    private LookupSnapshot load(Long userId) {
        defaultCategories.ensureDefaults(userId);
        List<LookupItem> accounts = accountRepository.findAllByUserIdAndActiveTrue(userId).stream()
                .filter(a -> a.getType() == AccountType.ASSET)
                .map(this::toItem)
                .toList();
        List<LookupItem> categories = categoryRepository.findAllByUserIdAndActiveTrue(userId).stream()
                .map(this::toItem)
                .toList();
        return new LookupSnapshot(accounts, categories);
    }

    private LookupItem toItem(Account account) {
        return new LookupItem(account.getId(), account.getName());
    }

    private LookupItem toItem(Category category) {
        return new LookupItem(category.getId(), category.getName());
    }

    private String key(Long userId) {
        return "ahorra:lookups:v1:" + userId;
    }
}
