package com.grab.store.catalog.internal.util;

import com.catalog.domain.aggregate.Category;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;

public final class CatalogPolicyValidator {

    private CatalogPolicyValidator() {
    }

    public static void validateCategoryPolicy(Category category) {
        if (!category.isActive() || !category.isListingAllowed()) {
            throw new CatalogServiceException(
                    new CatalogServiceError.CategoryListingProhibited(category.getId().getValue())
            );
        }
    }
}
