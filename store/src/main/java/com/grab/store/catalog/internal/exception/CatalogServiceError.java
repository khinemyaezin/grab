package com.grab.store.catalog.internal.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface CatalogServiceError extends MessageSource permits
        CatalogServiceError.ProductNotFound,
        CatalogServiceError.ProductNotFoundBySlug,
        CatalogServiceError.CategoryNotFound,
        CatalogServiceError.ParentCategoryNotFound,
        CatalogServiceError.ParentCategoryNotFoundForCategory,
        CatalogServiceError.VariantNotFound,
        CatalogServiceError.VariantNotFoundOrNotDeleted,
        CatalogServiceError.VariantDeletedCannotUpdate,
        CatalogServiceError.VariantUpdateFailed,
        CatalogServiceError.VariantAddFailed,
        CatalogServiceError.DuplicateVariantCombinationKey,
        CatalogServiceError.VariationCombinationNotFound,
        CatalogServiceError.SlugBlank {

    record ProductNotFound(String productId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "cat.service.product.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("productId", productId);
        }
    }

    record ProductNotFoundBySlug(String slug) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "cat.service.product.not_found_by_slug";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("slug", slug);
        }
    }

    record CategoryNotFound(String categoryId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "cat.service.category.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("categoryId", categoryId);
        }
    }

    record ParentCategoryNotFound(String parentCategoryId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "cat.service.category.parent_not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("parentCategoryId", parentCategoryId);
        }
    }

    record ParentCategoryNotFoundForCategory(String categoryId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "cat.service.category.parent_not_found_for_category";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("categoryId", categoryId);
        }
    }

    record VariantNotFound(String variantId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "cat.service.variant.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("variantId", variantId);
        }
    }

    record VariantNotFoundOrNotDeleted(String variantId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.service.variant.not_found_or_not_deleted";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("variantId", variantId);
        }
    }

    record VariantDeletedCannotUpdate(String variantId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.service.variant.deleted_cannot_update";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("variantId", variantId);
        }
    }

    record VariantUpdateFailed(String variantId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "cat.service.variant.update_failed";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("variantId", variantId);
        }
    }

    record VariantAddFailed(String variantId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "cat.service.variant.add_failed";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("variantId", variantId);
        }
    }

    record DuplicateVariantCombinationKey(String key) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "cat.service.variant.duplicate_combination_key";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("key", key);
        }
    }

    record VariationCombinationNotFound() implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "cat.service.variant.combination_not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record SlugBlank() implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "cat.service.product.slug_blank";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }
}
