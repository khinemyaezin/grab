package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.id.Id;
import com.grab.store.merchant.internal.exception.MerchantServiceError;
import com.grab.store.merchant.internal.exception.MerchantServiceException;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.repository.StorefrontRepository;

public final class StorefrontOwnership {
    private StorefrontOwnership() {
    }

    public static Storefront requireOwned(StorefrontRepository storefronts, Id storefrontId, Id merchantId) {
        Storefront storefront = storefronts.findById(storefrontId).orElseThrow(() -> notFound(storefrontId));
        if (!storefront.getMerchantId().equals(merchantId)) {
            throw notFound(storefrontId);
        }
        return storefront;
    }

    static MerchantServiceException notFound(Id storefrontId) {
        return new MerchantServiceException(
                new MerchantServiceError.StorefrontNotFound(storefrontId.getValue()),
                "Storefront not found"
        );
    }
}
