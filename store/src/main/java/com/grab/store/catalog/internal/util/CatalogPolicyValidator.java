package com.grab.store.catalog.internal.util;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;

public final class CatalogPolicyValidator {

    private CatalogPolicyValidator() {
    }

    public static void validateCategoryPolicy(Category category, Product product) {
        if (!category.isActive() || !category.isListingAllowed()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.CategoryListingProhibited(category.getId().getValue())
            );
        }
    }

    public static void validateActivationPolicy(Category category, Product product) {
        validateCategoryPolicy(category, product);
        if (category.isReviewRequired() && product.getStatus() != ProductStatus.IN_REVIEW) {
            throw new CatalogServiceException(
                    new CatalogServiceError.ProductReviewRequired(product.getId().getValue())
            );
        }
    }
}
