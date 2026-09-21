package com.pricing.application.model.read;

public record VariantPriceSetLinkResult(
        String variantId,
        String priceSetId,
        String productId,
        String sku,
        String merchantId
) {
}
