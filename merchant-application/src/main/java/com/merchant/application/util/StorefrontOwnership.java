package com.merchant.application.util;

import com.grab.framework.id.Id;
import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.port.outbound.StorefrontRepository;

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
