package com.ahorra.features.capture.service.impl;

import com.ahorra.common.exception.BadRequestException;
import com.ahorra.features.accounting.dto.LookupItem;
import com.ahorra.features.accounting.model.Account;
import com.ahorra.features.accounting.model.Category;
import com.ahorra.features.accounting.repository.AccountRepository;
import com.ahorra.features.accounting.repository.CategoryRepository;
import com.ahorra.features.accounting.service.impl.LookupCacheService;
import com.ahorra.features.capture.dto.request.CaptureRequest;
import com.ahorra.features.capture.dto.request.ParsedEntry;
import com.ahorra.features.capture.dto.request.ParsedEntryProposal;
import com.ahorra.features.capture.dto.request.ParsedItem;
import com.ahorra.features.capture.dto.response.PendingEntryResponse;
import com.ahorra.features.capture.enums.CaptureStatus;
import com.ahorra.features.capture.enums.EntrySource;
import com.ahorra.features.capture.enums.PendingStatus;
import com.ahorra.features.capture.model.PendingEntry;
import com.ahorra.features.capture.model.PendingEntryItem;
import com.ahorra.features.capture.model.RawEntry;
import com.ahorra.features.capture.parser.EntryParser;
import com.ahorra.features.capture.parser.ParseContext;
import com.ahorra.features.capture.parser.ParsedEntryValidator;
import com.ahorra.features.capture.repository.PendingEntryRepository;
import com.ahorra.features.capture.repository.RawEntryRepository;
import com.ahorra.features.capture.service.CaptureService;
import com.ahorra.features.day.service.DayGateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ahorra.common.constants.GeneralConstants.*;

@Slf4j
@Service
public class CaptureServiceImpl implements CaptureService {

    private final RawEntryRepository rawEntryRepository;
    private final PendingEntryRepository pendingEntryRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final LookupCacheService  lookupCacheService;
    private final DayGateService dayGateService;
    private final EntryParser entryParser;
    private final ParsedEntryValidator validator;
    private final ZoneId zone;

    private static final int MAX_ENTRIES_PER_NOTE = 10;

    public CaptureServiceImpl(RawEntryRepository rawEntryRepository,
                              PendingEntryRepository pendingEntryRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository,
                              LookupCacheService lookupCacheService, DayGateService dayGateService,
                              EntryParser entryParser,
                              ParsedEntryValidator validator,
                              @Value("${app.timezone:Africa/Lagos}") String timezone) {
        this.rawEntryRepository = rawEntryRepository;
        this.pendingEntryRepository = pendingEntryRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.lookupCacheService = lookupCacheService;
        this.dayGateService = dayGateService;
        this.entryParser = entryParser;
        this.validator = validator;
        this.zone = ZoneId.of(timezone);
    }

    @Override
    public List<PendingEntryResponse> capture(Long userId, CaptureRequest request) {

        dayGateService.assertNotGated(userId);

        RawEntry raw = getRawEntry(userId, request);

        List<PendingEntry> existing = pendingEntryRepository.findAllByRawEntryIdOrderByIdAsc(raw.getId());
        if (!existing.isEmpty()) {
            return existing.stream().map(this::toResponse).toList();
        }

        if (raw.getStatus() != CaptureStatus.RECEIVED)  {
            raw.setStatus(CaptureStatus.RECEIVED);
            raw.setFailureReason(null);
            raw = rawEntryRepository.save(raw);
        }

        try {
            LocalDate today = LocalDate.now(zone);
            List<LookupItem> accounts = lookupCacheService.captureAccounts(userId);
            List<LookupItem> categories = lookupCacheService.categories(userId);

            List<ParsedEntry> parseEntries = entryParser.parse(new ParseContext(
                    raw.getRawText(),
                    today,
                    accounts.stream().map(LookupItem::name).toList(),
                    categories.stream().map(LookupItem::name).toList()
            ));

            if (parseEntries == null || parseEntries.isEmpty()) {
                throw new BadRequestException("No amount were found in that entry");
            }

            if (parseEntries.size() > MAX_ENTRIES_PER_NOTE) {
                throw new BadRequestException("Too many separate entries in one note (max " + MAX_ENTRIES_PER_NOTE + ")");
            }

            List<PendingEntry> staged = new ArrayList<>();
            for (ParsedEntry entry : parseEntries) {
                ParsedEntryProposal proposal = validator.validateEntries(raw.getId(), entry, today, accounts, categories);
                staged.add(buildPending(userId, proposal));
            }

            List<PendingEntry> saved = pendingEntryRepository.saveAll(staged);
            raw.setStatus(CaptureStatus.PARSED);
            rawEntryRepository.save(raw);
            return saved.stream().map(this::toResponse).toList();
        } catch (RuntimeException ex) {
            markFailed(raw, ex.getMessage());
            throw ex;
        }
    }

    private String normalizeClientRequestId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmedValue = value.trim();
        if (trimmedValue.length() > MAX_CLIENT_REQUEST_ID_LENGTH) {
            throw new BadRequestException("clientRequestId is too long");
        }

        return trimmedValue;
    }

    private RawEntry findOrCreateRaw(Long userId, EntrySource source, String text, String clientRequestId) {
        if (clientRequestId != null) {
            Optional<RawEntry> foundEntry = rawEntryRepository.findByUserIdAndClientRequestId(userId, clientRequestId);
            if (foundEntry.isPresent()) {
                return foundEntry.get();
            }
        }

        RawEntry rawEntry = new RawEntry();
        rawEntry.setUserId(userId);
        rawEntry.setSource(source);
        rawEntry.setRawText(text);
        rawEntry.setStatus(CaptureStatus.RECEIVED);
        rawEntry.setClientRequestId(clientRequestId);
        try {
            return rawEntryRepository.save(rawEntry);
        } catch (DataIntegrityViolationException ex) {
            if (clientRequestId != null) {
                return rawEntryRepository.findByUserIdAndClientRequestId(userId, clientRequestId)
                        .orElseThrow(()-> ex);
            }
            throw ex;
        }
    }

    private PendingEntry buildPending(Long userId, ParsedEntryProposal proposal) {
        if (proposal.totalAmount().compareTo(MAX_TOTAL) >= 0) {
            throw new BadRequestException("Total amount is too large");
        }

        PendingEntry pendingEntry = new PendingEntry();
        pendingEntry.setUserId(userId);
        pendingEntry.setRawEntryId(proposal.rawEntryId());
        pendingEntry.setFlow(proposal.flow());
        pendingEntry.setEntryDate(proposal.entryDate());
        pendingEntry.setVendor(truncate(proposal.vendor(), MAX_VENDOR_LENGTH));
        pendingEntry.setCategoryId(proposal.categoryId());
        pendingEntry.setAccountId(proposal.accountId());
        pendingEntry.setTotalAmount(proposal.totalAmount());
        pendingEntry.setStatus(PendingStatus.PENDING);

        for (ParsedItem item : proposal.items()) {
            PendingEntryItem row = new PendingEntryItem();
            row.setDescription(item.description());
            row.setAmount(item.amount());
            pendingEntry.addItem(row);
        }

        return pendingEntry;
    }

    private PendingEntryResponse toResponse(PendingEntry pendingEntry) {
        String categoryName = pendingEntry.getCategoryId() == null ? null : categoryRepository.findById(pendingEntry.getCategoryId()).map(Category::getName).orElse(null);
        String accountName = pendingEntry.getAccountId() == null ? null : accountRepository.findById(pendingEntry.getAccountId()).map(Account::getName).orElse(null);

        List<ParsedItem> items = pendingEntry.getItems().stream()
                .map(item -> new ParsedItem(item.getDescription(), item.getAmount()))
                .toList();
        return new PendingEntryResponse(pendingEntry.getId(), pendingEntry.getRawEntryId(), pendingEntry.getStatus(), pendingEntry.getFlow(), pendingEntry.getEntryDate(), pendingEntry.getVendor(), pendingEntry.getCategoryId(), categoryName, pendingEntry.getAccountId(), accountName, items, pendingEntry.getTotalAmount());
    }

    private void markFailed(RawEntry raw, String reason) {
        try {
            raw.setStatus(CaptureStatus.FAILED);
            raw.setFailureReason(reason == null ? null : reason.substring(0, Math.min(reason.length(), 500)));
            rawEntryRepository.save(raw);
        } catch (RuntimeException ex) {
            log.error("Could not mark raw entry {} as failed", raw.getId(), ex);
        }
    }

    private String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }

    private RawEntry getRawEntry(Long userId, CaptureRequest request) {
        String text = request == null || request.text() == null ?  "" : request.text().trim();
        if (text.isEmpty()) {
            throw new BadRequestException("Entry text is required");
        }

        if (text.length() > MAX_TEXT_LENGTH) {
            throw new BadRequestException("Entry is too long (max " + MAX_TEXT_LENGTH + " characters)");
        }

        String clientRequestId = normalizeClientRequestId(request.clientRequestId());

        return findOrCreateRaw(userId, request.source(), text, clientRequestId);
    }

}
