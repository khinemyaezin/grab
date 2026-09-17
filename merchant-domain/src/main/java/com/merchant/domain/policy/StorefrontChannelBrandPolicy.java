package com.merchant.domain.policy;

import com.grab.framework.id.Id;
import com.merchant.domain.aggregate.StorefrontChannelBrand;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;

import java.util.Objects;

public final class StorefrontChannelBrandPolicy {

    public void requireAttachable(StorefrontChannelBrand existing, Id salesChannelId) {
        Id requiredChannelId = Objects.requireNonNull(salesChannelId, "salesChannelId is required");
        if (existing == null) {
            return;
        }
        if (!existing.getSalesChannelId().equals(requiredChannelId)) {
            throw new MerchantDomainException(
                    new MerchantDomainError.StorefrontChannelAlreadyAttached(existing.getStorefrontId().getValue()),
                    "Storefront is already attached to a sales channel"
            );
        }
    }
}
