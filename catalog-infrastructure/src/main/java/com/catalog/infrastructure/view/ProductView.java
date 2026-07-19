package com.catalog.infrastructure.view;

import com.catalog.domain.valueobject.ProductStatus;

public record ProductView(
        String id,
        String name,
        String status,
        String slug,
        String categoryId
) {
    public ProductView(
            String id,
            String name,
            ProductStatus status,
            String slug,
            String categoryId
    ) {
        this(
                id,
                name,
                status != null ? status.name() : null,
                slug,
                categoryId
        );
    }
}
