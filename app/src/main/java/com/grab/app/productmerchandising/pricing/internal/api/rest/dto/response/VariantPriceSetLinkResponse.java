package com.grab.app.productmerchandising.pricing.internal.api.rest.dto.response;

public record VariantPriceSetLinkResponse(
        String variantId,
        String priceSetId,
        String productId,
        String sku,
        String merchantId
) {
}
