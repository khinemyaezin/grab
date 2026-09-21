package com.catalog.application.model.read;

public record FindPublishedVariantResult(
        String variantId,
        String productId,
        String sellerId,
        String sku,
        String title,
        String slug,
        String productStatus,
        boolean untracked,
        String media
) {
    public boolean active() {
        return "ACTIVE".equals(productStatus);
    }
}
