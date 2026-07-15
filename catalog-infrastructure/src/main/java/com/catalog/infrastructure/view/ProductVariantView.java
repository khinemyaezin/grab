package com.catalog.infrastructure.view;

import com.catalog.domain.valueobject.ProductStatus;

public record ProductVariantView(
        String productId,
        String variantId,
        String sku,
        String status,
        String slug,
        String categoryId,
        String productName
) {
    public ProductVariantView(
            String productId,
            String variantId,
            String sku,
            ProductStatus status,
            String slug,
            String categoryId,
            String productName
    ) {
        this(
                productId,
                variantId,
                sku,
                status != null ? status.name() : null,
                slug,
                categoryId,
                productName
        );
    }
}
