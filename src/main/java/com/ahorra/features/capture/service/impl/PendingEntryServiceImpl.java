package com.ahorra.features.capture.service.impl;

import com.ahorra.common.exception.BadRequestException;
import com.ahorra.common.money.MoneyUtils;
import com.ahorra.features.accounting.dto.PostEntryLine;
import com.ahorra.features.accounting.dto.PostEntryRequest;
import com.ahorra.features.accounting.enums.AccountType;
import com.ahorra.features.accounting.enums.EntryDirection;
import com.ahorra.features.accounting.model.Account;
import com.ahorra.features.accounting.model.Category;
import com.ahorra.features.accounting.model.LedgerEntry;
import com.ahorra.features.accounting.repository.AccountRepository;
import com.ahorra.features.accounting.repository.CategoryRepository;
import com.ahorra.features.accounting.service.LedgerService;
import com.ahorra.features.accounting.service.impl.SystemAccountService;
import com.ahorra.features.capture.dto.request.ConfirmRequest;
import com.ahorra.features.capture.dto.request.ParsedItem;
import com.ahorra.features.capture.dto.response.PendingEntryResponse;
import com.ahorra.features.capture.enums.FlowDirection;
import com.ahorra.features.capture.enums.PendingStatus;
import com.ahorra.features.capture.model.PendingEntry;
import com.ahorra.features.capture.model.PendingEntryItem;
import com.ahorra.features.capture.parser.ParsedEntryValidator;
import com.ahorra.features.capture.repository.PendingEntryRepository;
import com.ahorra.features.capture.service.PendingEntryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PendingEntryServiceImpl implements PendingEntryService {

    private static final int MAX_VENDOR_LENGTH = 255;
    private static final int MAX_LEDGER_DESCRIPTION_LENGTH = 500;
    private static final BigDecimal MAX_TOTAL = new BigDecimal("10000000000000");

    private final PendingEntryRepository pendingEntryRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final SystemAccountService systemAccountService;
    private final LedgerService ledgerService;
    private final ParsedEntryValidator parsedEntryValidator;
    private final PendingEntryMapper pendingEntryMapper;
    private final ZoneId zone;

    public PendingEntryServiceImpl(PendingEntryRepository pendingEntryRepository,
                                   AccountRepository accountRepository,
                                   CategoryRepository categoryRepository,
                                   SystemAccountService systemAccountService,
                                   LedgerService ledgerService,
                                   ParsedEntryValidator parsedEntryValidator,
                                   PendingEntryMapper pendingEntryMapper,
                                   @Value("${app.timeZone:Africa/Lagos}") String timeZone) {
        this.pendingEntryRepository = pendingEntryRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.systemAccountService = systemAccountService;
        this.ledgerService = ledgerService;
        this.parsedEntryValidator = parsedEntryValidator;
        this.pendingEntryMapper = pendingEntryMapper;
        this.zone = ZoneId.of(timeZone);
    }


    @Override
    public List<PendingEntryResponse> listPending(Long userId) {
        return pendingEntryMapper.toResponse(pendingEntryRepository.findAllByUserIdAndStatusOrderByEntryDateAscIdAsc(userId, PendingStatus.PENDING));
    }

    @Override
    @Transactional
    public PendingEntryResponse confirm(Long userId, Long pendingEntryId, ConfirmRequest request) {
        PendingEntry pendingEntry = pendingEntryRepository.findForUpdate(pendingEntryId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Pending entry not found"));

        boolean edited = request != null && request.hasEdits();

        if (pendingEntry.getStatus() != PendingStatus.PENDING) {
            if (edited) {
                throw new BadRequestException("This entry has already been confirmed and can no longer be edited");
            }
            return pendingEntryMapper.toResponse(pendingEntry);
        }

        if (edited) {
            applyEdits(userId, pendingEntry, request);
        }

        LedgerEntry ledgerEntry = ledgerService.postEntry(buildPostRequest(userId, pendingEntry));
        pendingEntry.markPosted(ledgerEntry.getId(), edited);
        PendingEntry savedEntry = pendingEntryRepository.save(pendingEntry);

        log.info("Pending entry {} posted as ledger entry {} for user {}", pendingEntry.getId(), ledgerEntry.getId(), userId);
        return pendingEntryMapper.toResponse(savedEntry);
    }

    @Override
    @Transactional
    public PendingEntryResponse discard(Long userId, Long pendingEntryId) {
        PendingEntry pendingEntry = pendingEntryRepository.findForUpdate(pendingEntryId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Pending entry not found"));

        if (pendingEntry.getStatus() == PendingStatus.DISCARDED) {
            return pendingEntryMapper.toResponse(pendingEntry);
        }

        if (pendingEntry.getStatus() != PendingStatus.PENDING) {
            throw new BadRequestException("This entry has already been confirmed and can no longer be discarded");
        }

        pendingEntry.markDiscarded();
        pendingEntryRepository.save(pendingEntry);

        log.info("Pending entry {} discarded for user {}", pendingEntry.getId(), userId);
        return pendingEntryMapper.toResponse(pendingEntry);
    }

    @Override
    @Transactional
    public PendingEntryResponse restore(Long userId, Long pendingEntryId) {
        PendingEntry pendingEntry = pendingEntryRepository.findForUpdate(pendingEntryId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Pending entry not found"));

        if (pendingEntry.getStatus() == PendingStatus.PENDING) {
            return pendingEntryMapper.toResponse(pendingEntry);
        }

        if (pendingEntry.getStatus() != PendingStatus.DISCARDED) {
            throw new BadRequestException("Only discarded entry can be restored");
        }

        LocalDate entryDate = pendingEntry.getEntryDate();
        pendingEntry.restore(entryDate);
        pendingEntryRepository.save(pendingEntry);

        log.info("Pending entry {} restored for user {}", pendingEntry.getId(), userId);
        return pendingEntryMapper.toResponse(pendingEntry);
    }

    private void applyEdits(Long userId, PendingEntry pending, ConfirmRequest edits) {
        if (edits.flow() != null) {
            pending.setFlow(edits.flow());
        }
        if (edits.entryDate() != null) {
            if (edits.entryDate().isAfter(LocalDate.now(zone))) {
                throw new BadRequestException("Entry date cannot be in the future");
            }
            pending.setEntryDate(edits.entryDate());
        }
        if (edits.vendor() != null) {
            String vendor = edits.vendor().trim();
            pending.setVendor(vendor.isEmpty() ? null : truncate(vendor, MAX_VENDOR_LENGTH));
        }
        if (edits.categoryId() != null) {
            categoryRepository.findByIdAndUserId(edits.categoryId(), userId)
                    .filter(Category::isActive)
                    .orElseThrow(() -> new BadRequestException("Category not found or inactive"));
            pending.setCategoryId(edits.categoryId());
        }
        if (edits.accountId() != null) {
            accountRepository.findByIdAndUserId(edits.accountId(), userId)
                    .filter(Account::isActive)
                    .filter(a -> a.getType() == AccountType.ASSET)
                    .orElseThrow(() -> new BadRequestException("Account not found, inactive, or not a cash/bank account"));
            pending.setAccountId(edits.accountId());
        }
        if (edits.items() != null) {
            List<ParsedItem> clean = parsedEntryValidator.validateItems(edits.items());
            BigDecimal total = clean.stream().map(ParsedItem::amount).reduce(BigDecimal.ZERO, MoneyUtils::add);
            if (total.compareTo(MAX_TOTAL) >= 0) {
                throw new BadRequestException("Total amount is too large");
            }
            pending.getItems().clear();
            for (ParsedItem item : clean) {
                PendingEntryItem row = new PendingEntryItem();
                row.setDescription(item.description());
                row.setAmount(item.amount());
                pending.addItem(row);
            }
            pending.setTotalAmount(total);
        }
    }

    private PostEntryRequest buildPostRequest(Long userId, PendingEntry pending) {
        Long paymentAccountId = pending.getAccountId() != null ? pending.getAccountId() : systemAccountService.cash(userId).getId();
        BigDecimal total = pending.getTotalAmount();

        List<PostEntryLine> lines;
        if (pending.getFlow() == FlowDirection.EXPENSE) {
            Long expensesId = systemAccountService.expenses(userId).getId();
            lines = List.of(new PostEntryLine(expensesId, EntryDirection.DEBIT, total), new PostEntryLine(paymentAccountId, EntryDirection.CREDIT, total));
        } else {
            Long incomeId = systemAccountService.income(userId).getId();
            lines = List.of(new PostEntryLine(paymentAccountId, EntryDirection.DEBIT, total), new PostEntryLine(incomeId, EntryDirection.CREDIT, total));
        }

        return new PostEntryRequest(userId, pending.getCategoryId(), pending.getEntryDate(), buildDescription(pending), lines);
    }

    private String buildDescription(PendingEntry pending) {
        String items = pending.getItems().stream()
                .map(PendingEntryItem::getDescription)
                .collect(Collectors.joining(", "));
        String description = pending.getVendor() == null ? items : pending.getVendor() + " - " + items;
        return truncate(description, MAX_LEDGER_DESCRIPTION_LENGTH);
    }

    private String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
