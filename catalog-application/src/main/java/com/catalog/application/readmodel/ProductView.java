package com.catalog.application.readmodel;

import com.catalog.domain.valueobject.ProductStatus;

public record ProductView(
        String id,
        String name,
        String status,
        String slug,
        String categoryId,
        String merchantId,
        boolean featured,
        String condition
) {
    public ProductView(
            String id,
            String name,
            String status,
            String slug,
            String categoryId
    ) {
        this(id, name, status, slug, categoryId, null, false, null);
    }

    public ProductView(
            String id,
            String name,
            ProductStatus status,
            String slug,
            String categoryId
    ) {
        this(id, name, status != null ? status.name() : null, slug, categoryId, null, false, null);
    }

    public ProductView(
            String id,
            String name,
            ProductStatus status,
            String slug,
            String categoryId,
            String merchantId,
            Boolean featured,
            String condition
    ) {
        this(
                id,
                name,
                status != null ? status.name() : null,
                slug,
                categoryId,
                merchantId,
                featured != null && featured,
                condition
        );
    }
}
