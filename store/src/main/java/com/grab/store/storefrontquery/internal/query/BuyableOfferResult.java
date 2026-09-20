package com.grab.store.storefrontquery.internal.query;

import java.math.BigDecimal;

public record BuyableOfferResult(
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
