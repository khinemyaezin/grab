package com.merchant.domain.enums;

public enum MemberStatus {
    INVITED,
    ACTIVE,
    REMOVED;

    public boolean isInvited() {
        return this == INVITED;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isRemoved() {
        return this == REMOVED;
    }
}
