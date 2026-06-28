package com.merchant.domain.enums;

public enum MerchantStatus {
    DRAFT,
    PENDING_REVIEW,
    CHANGES_REQUESTED,
    ACTIVE,
    SUSPENDED,
    REJECTED,
    CLOSED;

    public boolean isTerminal() {
        return this == REJECTED || this == CLOSED;
    }

    public boolean isApplicationOpen() {
        return this == DRAFT || this == PENDING_REVIEW || this == CHANGES_REQUESTED;
    }
}
