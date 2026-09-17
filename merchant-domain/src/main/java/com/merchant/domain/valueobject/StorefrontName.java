package com.merchant.domain.valueobject;

import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;

public record StorefrontName(String value) {
    private static final int MAX_LENGTH = 255;

    public StorefrontName {
        if (value == null || value.isBlank()) {
            throw new MerchantDomainException(
                    new MerchantDomainError.InvalidField("name"),
                    "Storefront name is required"
            );
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new MerchantDomainException(
                    new MerchantDomainError.InvalidField("name"),
                    "Storefront name must not exceed " + MAX_LENGTH + " characters"
            );
        }
    }
}
