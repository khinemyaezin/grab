package com.catalog.infrastructure.view;

public record ProductVariantRefView(
        String productId,
        String variantId,
        String sku
) {
}
