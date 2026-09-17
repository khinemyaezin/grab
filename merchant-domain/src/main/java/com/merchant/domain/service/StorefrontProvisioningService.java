package com.merchant.domain.service;

import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;

import java.util.Objects;

public final class StorefrontProvisioningService {
    public void requireOperational(MerchantAccount merchant) {
        Objects.requireNonNull(merchant, "merchant is required");
        if (!merchant.isOperational()) {
            throw new MerchantDomainException(
                    new MerchantDomainError.MerchantNotOperational(
                            merchant.getId().getValue(),
                            merchant.getStatus().name()
                    ),
                    "Merchant is not operational"
            );
        }
    }
}
