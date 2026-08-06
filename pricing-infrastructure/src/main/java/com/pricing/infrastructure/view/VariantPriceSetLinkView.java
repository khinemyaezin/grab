package com.pricing.infrastructure.view;

import java.time.Instant;

public record VariantPriceSetLinkView(
        String variantId,
        String priceSetId,
        String productId,
        String sku,
        String merchantId,
        Instant createdAt,
        Instant updatedAt
) {
}
