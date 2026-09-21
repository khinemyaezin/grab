package com.catalog.application.model.read;

public record ProductVariantRefView(
        String productId,
        String variantId,
        String sku
) {
}
