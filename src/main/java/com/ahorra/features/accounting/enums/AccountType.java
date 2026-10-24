package com.ahorra.features.accounting.enums;

public enum AccountType {
    ASSET(true),
    EXPENSE(true),
    LIABILITY(true),
    EQUITY(false),
    INCOME(false);

    private boolean debitNormal;

    AccountType(boolean debitNormal) {
        this.debitNormal = debitNormal;
    }

    public boolean isDebitNormal() {
        return debitNormal;
    }
}
