package com.grab.store.pricing.queries;

public record VariantPriceSetLinkResult(
        String variantId,
        String priceSetId,
        String productId,
        String sku,
        String merchantId
) {
}
