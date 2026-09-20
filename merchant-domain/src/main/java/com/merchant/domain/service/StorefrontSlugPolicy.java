package com.merchant.domain.service;

import com.grab.framework.id.Id;
import com.merchant.domain.exception.MerchantDomainError;
import com.merchant.domain.exception.MerchantDomainException;
import com.merchant.domain.port.outbound.StorefrontRepository;
import com.merchant.domain.valueobject.StorefrontSlug;

import java.util.Objects;

public final class StorefrontSlugPolicy {
    private final StorefrontRepository storefronts;

    public StorefrontSlugPolicy(StorefrontRepository storefronts) {
        this.storefronts = Objects.requireNonNull(storefronts, "storefront repository is required");
    }

    public void requireAvailable(StorefrontSlug slug, Id excludingId) {
        if (storefronts.existsSlug(slug, excludingId)) {
            throw new MerchantDomainException(
                    new MerchantDomainError.DuplicateSlug(slug.value()),
                    "Storefront slug is already used"
            );
        }
    }
}
