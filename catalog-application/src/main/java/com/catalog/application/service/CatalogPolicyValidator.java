package com.catalog.application.service;

import com.catalog.domain.aggregate.Category;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;

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
