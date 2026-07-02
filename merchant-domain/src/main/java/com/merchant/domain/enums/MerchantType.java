package com.merchant.domain.enums;

public enum MerchantType {
    FIRST_PARTY_RETAILER,
    THIRD_PARTY,
    C2C_SELLER;

    public boolean requiresBusinessRegistration() {
        return switch (this) {
            case FIRST_PARTY_RETAILER, THIRD_PARTY -> true;
            case C2C_SELLER -> false;
        };
    }
}
