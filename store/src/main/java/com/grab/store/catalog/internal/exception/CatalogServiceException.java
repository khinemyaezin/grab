package com.grab.store.catalog.internal.exception;

import com.grab.framework.exception.DomainException;

public class CatalogServiceException extends DomainException {

    public CatalogServiceException(CatalogServiceError error) {
        super(error, defaultMessage(error));
    }

    public CatalogServiceException(CatalogServiceError error, String defaultMessage) {
        super(error, defaultMessage);
    }

    private static String defaultMessage(CatalogServiceError error) {
        return switch (error) {
            case CatalogServiceError.ProductNotFound e ->
                    "Product not found: " + e.productId();
            case CatalogServiceError.ProductNotFoundBySlug e ->
                    "Product not found for slug: " + e.slug();
            case CatalogServiceError.CategoryNotFound e ->
                    "Category not found: " + e.categoryId();
            case CatalogServiceError.ParentCategoryNotFound e ->
                    "Parent category not found: " + e.parentCategoryId();
            case CatalogServiceError.ParentCategoryNotFoundForCategory e ->
                    "Parent category not found for category: " + e.categoryId();
            case CatalogServiceError.VariantNotFound e ->
                    "Variant not found: " + e.variantId();
            case CatalogServiceError.VariantNotFoundOrNotDeleted e ->
                    "Variant not found or not deleted: " + e.variantId();
            case CatalogServiceError.VariantDeletedCannotUpdate e ->
                    "Cannot update deleted variant: " + e.variantId();
            case CatalogServiceError.VariantUpdateFailed e ->
                    "Failed to update variant: " + e.variantId();
            case CatalogServiceError.VariantAddFailed e ->
                    "Failed to add variant: " + e.variantId();
            case CatalogServiceError.DuplicateVariantCombinationKey e ->
                    "Duplicate request variant combination key: " + e.key();
            case CatalogServiceError.VariationCombinationNotFound ignored ->
                    "Variation combination not found";
            case CatalogServiceError.SlugBlank ignored ->
                    "Slug cannot be blank";
        };
    }
}
