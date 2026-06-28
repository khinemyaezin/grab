package com.merchant.domain.valueobject;

import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;

public record MerchantName(String legalName, String displayName) {
    public MerchantName {
        legalName = normalizeOptional(legalName);
        displayName = normalizeRequired(displayName, "displayName");
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String normalizeRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new MerchantDomainException(new MerchantDomainError.InvalidField(field), field + " is required");
        }
        return value.trim();
    }
}
