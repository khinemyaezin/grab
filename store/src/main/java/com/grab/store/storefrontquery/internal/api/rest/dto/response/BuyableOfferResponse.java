package com.grab.store.storefrontquery.internal.api.rest.dto.response;

import java.math.BigDecimal;

public record BuyableOfferResponse(
        String salesChannelId,
        String variantId,
        String productId,
        String sellerId,
        String sku,
        String title,
        String slug,
        String media,
        String productStatus,
        BigDecimal amount,
        String currency,
        int availableQty,
        boolean untracked,
        boolean buyable
) {
}
