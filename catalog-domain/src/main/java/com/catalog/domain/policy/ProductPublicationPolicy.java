package com.catalog.domain.policy;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.exception.CatalogDomainError;
import com.catalog.domain.exception.CatalogDomainValidationException;
import com.catalog.domain.valueobject.ProductStatus;

import java.util.Objects;

public final class ProductPublicationPolicy {

    public void requirePublishable(Product product) {
        Product required = Objects.requireNonNull(product, "product is required");
        if (required.getStatus() != ProductStatus.ACTIVE) {
            throw new CatalogDomainValidationException(
                    new CatalogDomainError.ProductNotPublishable(required.getStatus().name()),
                    "Product must be ACTIVE to publish to a sales channel"
            );
        }
    }
}
