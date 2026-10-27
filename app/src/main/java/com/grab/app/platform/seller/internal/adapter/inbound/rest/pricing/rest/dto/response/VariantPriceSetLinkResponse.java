package com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.response;

public record VariantPriceSetLinkResponse(
        String variantId,
        String priceSetId,
        String productId,
        String sku,
        String merchantId
) {
}
