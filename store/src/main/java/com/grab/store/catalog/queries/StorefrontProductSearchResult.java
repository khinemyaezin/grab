package com.grab.store.catalog.queries;

import java.util.List;

public record StorefrontProductSearchResult(
        String productId,
        String productName,
        String slug,
        String categoryName,
        String categoryId,
        String condition,
        boolean featured,
        String merchantId,
        Media thumbnail,
        List<VariantRef> variants
) {
    public record Media(
            String id,
            String storageKey,
            String url,
            String contentType,
            int rank
    ) {
    }

    public record VariantRef(
            String variantId,
            String sku
    ) {
    }
}
