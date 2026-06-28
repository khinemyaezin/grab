package com.merchant.domain.valueobject;

import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;

public record LifecycleReason(String value) {
    public LifecycleReason {
        if (value == null || value.isBlank() || value.trim().length() > 1000) {
            throw new MerchantDomainException(
                    new MerchantDomainError.InvalidField("lifecycleReason"),
                    "Lifecycle reason is required and must not exceed 1000 characters"
            );
        }
        value = value.trim();
    }
}
