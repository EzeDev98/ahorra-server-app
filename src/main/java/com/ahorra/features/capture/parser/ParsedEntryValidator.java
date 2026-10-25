package com.ahorra.features.capture.parser;

import com.ahorra.common.exception.BadRequestException;
import com.ahorra.common.money.MoneyUtils;
import com.ahorra.features.accounting.dto.LookupItem;
import com.ahorra.features.capture.dto.request.ParsedEntry;
import com.ahorra.features.capture.dto.request.ParsedEntryProposal;
import com.ahorra.features.capture.dto.request.ParsedItem;
import com.ahorra.features.capture.enums.FlowDirection;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static com.ahorra.common.constants.GeneralConstants.*;
import static com.ahorra.common.money.MoneyConfig.MONETARY_SCALE;

@Component
public class ParsedEntryValidator {

    public ParsedEntryProposal validateEntries(Long rawEntryId, ParsedEntry parsed, LocalDate today, List<LookupItem> accounts, List<LookupItem> categories) {
        FlowDirection flow = parseFlow(parsed.flow());
        List<ParsedItem> items = validateItems(parsed.items());
        BigDecimal total = items.stream().map(ParsedItem::amount).reduce(BigDecimal.ZERO, MoneyUtils::add);

        LookupItem itemCategory = parsed.categoryName() == null ? null : categories.stream()
                .filter(category -> category.name().trim().equalsIgnoreCase(parsed.categoryName()))
                .findFirst().orElse(null);

        LookupItem userAccount = parsed.accountName() == null ? null : accounts.stream()
                .filter(account -> account.name().trim().equalsIgnoreCase(parsed.accountName()))
                .findFirst().orElse(null);

        return new ParsedEntryProposal(
                rawEntryId,
                flow,
                resolveDate(parsed.entryDate(), today),
                parsed.vendor(),
                itemCategory == null ? null : itemCategory.id(),
                itemCategory == null ? null : itemCategory.name(),
                userAccount == null ? null : userAccount.id(),
                userAccount == null ? null : userAccount.name(),
                items,
                total);
    }

    private FlowDirection parseFlow(String flow) {
        try {
            return FlowDirection.valueOf(flow == null ? "" : flow.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Could not tell whether this was money spent or received");
        }

    }

    private List<ParsedItem> validateItems(List<ParsedItem> items) {

        if (items == null || items.isEmpty()) {
            throw new BadRequestException("No amounts were found in that entry");
        }

        if (items.size() > MAX_ITEMS) {
            throw new BadRequestException("No amount were found in tha entry");
        }

        List<ParsedItem> clean = new ArrayList<>();
        for (ParsedItem item : items) {
            String description = item.description();
            BigDecimal amount = item.amount();
            if (description == null || description.isBlank()) {
                throw new BadRequestException("Every item needs a description");
            }

            if (amount == null || amount.stripTrailingZeros().scale() > MONETARY_SCALE) {
                throw new BadRequestException("Could not read an amount for: " + description);
            }

            BigDecimal normalizedAmount = MoneyUtils.normalize(amount);
            if (!MoneyUtils.isPositive(normalizedAmount) || normalizedAmount.compareTo(MAX_AMOUNT) >= 0) {
                throw new BadRequestException("Invalid amount for: " + description);
            }

            String trimmedDescription = description.trim();
            if (trimmedDescription.length() > MAX_DESCRIPTION_LENGTH) {
                trimmedDescription = trimmedDescription.substring(0, MAX_DESCRIPTION_LENGTH);
            }
            clean.add(new ParsedItem(trimmedDescription, normalizedAmount));
        }

        return clean;
    }

    private LocalDate resolveDate(String value, LocalDate today) {
        if (value == null) {
            return today;
        }
        try {
            LocalDate date = LocalDate.parse(value.trim());
            return date.isAfter(today) ? today : date;
        } catch (DateTimeParseException ex) {
            return today;
        }
    }
}
