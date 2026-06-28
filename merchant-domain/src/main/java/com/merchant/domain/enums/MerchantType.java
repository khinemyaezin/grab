package com.merchant.domain.enums;

public enum MerchantType {
    RETAILER,
    THIRD_PARTY,
    CONSUMER;

    public boolean requiresBusinessRegistration() {
        return this != CONSUMER;
    }
}
