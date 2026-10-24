package com.ahorra.features.accounting.service.impl;

import com.ahorra.common.exception.BadRequestException;
import com.ahorra.common.money.MoneyUtils;
import com.ahorra.features.accounting.dto.PostEntryLine;
import com.ahorra.features.accounting.dto.PostEntryRequest;
import com.ahorra.features.accounting.enums.EntryDirection;
import com.ahorra.features.accounting.model.Account;
import com.ahorra.features.accounting.model.LedgerEntry;
import com.ahorra.features.accounting.model.TransactionEntry;
import com.ahorra.features.accounting.repository.AccountRepository;
import com.ahorra.features.accounting.repository.CategoryRepository;
import com.ahorra.features.accounting.repository.LedgerEntryRepository;
import com.ahorra.features.accounting.service.LedgerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ahorra.common.money.MoneyConfig.MONETARY_SCALE;

@Slf4j
@RequiredArgsConstructor
@Service
public class LedgerServiceImpl implements LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public LedgerEntry postEntry(PostEntryRequest request) {
        validateHeader(request);
        List<PostEntryLine> lines = request.lines();
        validateLines(lines);
        validateAccounts(request.userId(), lines);
        validateBalanced(lines);

        LedgerEntry entry = new LedgerEntry();
        entry.setUserId(request.userId());
        entry.setCategoryId(request.categoryId());
        entry.setEntryDate(request.entryDate());
        entry.setDescription(request.description());

        for (PostEntryLine line : lines) {
            TransactionEntry transactionEntry = new TransactionEntry();
            transactionEntry.setAccountId(line.accountId());
            transactionEntry.setDirection(line.direction());
            transactionEntry.setAmount(MoneyUtils.normalize(line.amount()));
            entry.addLine(transactionEntry);
        }

        LedgerEntry saved = ledgerEntryRepository.save(entry);
        log.info("Posted ledger entry {} for user {} with {} lines", saved.getId(), request.userId(), lines.size());
        return saved;
    }

    private void validateHeader(PostEntryRequest request) {
        if (request == null || request.userId() == null) {
            throw new BadRequestException("User is required to post a ledger entry");
        }
        if (request.entryDate() == null) {
            throw new BadRequestException("Entry date is required");
        }
        if (request.categoryId() != null && categoryRepository.findByIdAndUserId(request.categoryId(), request.userId())
                .filter(category -> category.isActive()).isEmpty()) {
            throw new BadRequestException("Category not found or inactive");
        }
    }

    private void validateLines(List<PostEntryLine> lines) {
        if (lines == null || lines.size() < 2) {
            throw new BadRequestException("A ledger entry needs at least two lines");
        }
        for (PostEntryLine line : lines) {
            if (line == null || line.accountId() == null || line.direction() == null) {
                throw new BadRequestException("Every line needs an account and a direction");
            }
            BigDecimal amount = line.amount();
            if (amount == null || amount.stripTrailingZeros().scale() > MONETARY_SCALE) {
                throw new BadRequestException("Amounts must have at most " + MONETARY_SCALE + " decimal places");
            }
            if (!MoneyUtils.isPositive(MoneyUtils.normalize(amount))) {
                throw new BadRequestException("Every line amount must be greater than zero");
            }
        }
        boolean hasDebit = lines.stream().anyMatch(line -> line.direction() == EntryDirection.DEBIT);
        boolean hasCredit = lines.stream().anyMatch(line -> line.direction() == EntryDirection.CREDIT);
        if (!hasDebit || !hasCredit) {
            throw new BadRequestException("A ledger entry needs at least one debit and one credit");
        }
    }

    private void validateAccounts(Long userId, List<PostEntryLine> lines) {
        Set<Long> accountIds = lines.stream().map(PostEntryLine::accountId).collect(Collectors.toSet());
        Map<Long, Account> found = accountRepository.findAllByUserIdAndIdIn(userId, accountIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
        if (found.size() != accountIds.size()) {
            throw new BadRequestException("One or more accounts do not exist for this user");
        }
        if (found.values().stream().anyMatch(account -> !account.isActive())) {
            throw new BadRequestException("One or more accounts are inactive");
        }
    }

    private void validateBalanced(List<PostEntryLine> lines) {
        BigDecimal debits = sum(lines, EntryDirection.DEBIT);
        BigDecimal credits = sum(lines, EntryDirection.CREDIT);
        if (!MoneyUtils.equals(debits, credits)) {
            throw new BadRequestException("Entry is not balanced: debits " + debits + " != credits " + credits);
        }
    }

    private BigDecimal sum(List<PostEntryLine> lines, EntryDirection direction) {
        return lines.stream()
                .filter(line -> line.direction() == direction)
                .map(PostEntryLine::amount)
                .reduce(BigDecimal.ZERO, MoneyUtils::add);
    }
}
