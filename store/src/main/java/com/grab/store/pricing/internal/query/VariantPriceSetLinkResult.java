package com.grab.store.pricing.internal.query;

public record VariantPriceSetLinkResult(
        String variantId,
        String priceSetId,
        String productId,
        String sku,
        String merchantId
) {
}
