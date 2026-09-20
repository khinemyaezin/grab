package com.catalog.application.readmodel;

public record ProductVariantRefView(
        String productId,
        String variantId,
        String sku
) {
}
