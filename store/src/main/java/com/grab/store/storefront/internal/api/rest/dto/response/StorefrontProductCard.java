package com.grab.store.storefront.internal.api.rest.dto.response;

import java.math.BigDecimal;

public record StorefrontProductCard(
        String productId,
        String productName,
        String slug,
        String categoryName,
        String categoryId,
        String condition,
        boolean featured,
        String thumbnailUrl,
        PriceRange priceRange,
        boolean inStock,
        String sellerId
) {
    public record PriceRange(
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String currencyCode
    ) {
    }
}
