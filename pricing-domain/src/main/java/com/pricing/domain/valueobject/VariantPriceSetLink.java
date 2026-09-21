package com.pricing.domain.valueobject;

import java.time.Instant;

public record VariantPriceSetLink(
        String variantId,
        String priceSetId,
        String productId,
        String sku,
        String merchantId,
        Instant createdAt,
        Instant updatedAt
) {
}
