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
        CatalogServiceError.CategoryHasAssignedProducts,
        CatalogServiceError.CategoryListingProhibited,
        CatalogServiceError.CategoryC2CNotAllowed,
        CatalogServiceError.ProductReviewRequired,
        CatalogServiceError.VariantNotFound,
        CatalogServiceError.VariantNotFoundOrNotDeleted,
        CatalogServiceError.VariantDeletedCannotUpdate,
        CatalogServiceError.VariantUpdateFailed,
        CatalogServiceError.VariantAddFailed,
        CatalogServiceError.SkuAlreadyExists,
        CatalogServiceError.DuplicateVariantCombinationKey,
        CatalogServiceError.VariationCombinationNotFound,
        CatalogServiceError.SlugBlank,
        CatalogServiceError.ProductDescriptionNotFound,
        CatalogServiceError.ProductMediaNotFound,
        CatalogServiceError.InvalidProductDescriptionPatch,
        CatalogServiceError.InvalidProductMediaPatch,
        CatalogServiceError.ProductAlreadyExisted,
        CatalogServiceError.MerchantScopeRequired,
        CatalogServiceError.EmptyVariantOverrides,
        CatalogServiceError.InvalidEnumValue {

    record MerchantScopeRequired(String scopeKey, String scopeId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "cat.service.merchant_scope.required";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("scopeKey", scopeKey, "scopeId", scopeId);
        }
    }

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

    record CategoryHasAssignedProducts(String categoryId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.service.category.has_assigned_products";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("categoryId", categoryId);
        }
    }

    record CategoryListingProhibited(String categoryId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.service.category.listing_prohibited";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("categoryId", categoryId);
        }
    }

    record CategoryC2CNotAllowed(String categoryId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.service.category.c2c_not_allowed";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("categoryId", categoryId);
        }
    }

    record ProductReviewRequired(String productId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.service.product.review_required";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("productId", productId);
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
            return Map.of("sku", variantId);
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
            return Map.of("sku", variantId);
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
            return Map.of("sku", variantId);
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
            return Map.of("sku", variantId);
        }
    }

    record VariantAddFailed(String sku) implements CatalogServiceError {
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
            return Map.of("sku", sku);
        }
    }

    record SkuAlreadyExists(String sku) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "cat.service.variant.sku_already_exists";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("sku", sku);
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

    record ProductDescriptionNotFound(String descriptionId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "cat.service.product.description_not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("descriptionId", descriptionId);
        }
    }

    record ProductMediaNotFound(String mediaId) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "cat.service.product.media_not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("mediaId", mediaId);
        }
    }

    record InvalidProductDescriptionPatch(String reason) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "cat.service.product.description_patch_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("reason", reason);
        }
    }

    record InvalidProductMediaPatch(String reason) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "cat.service.product.media_patch_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("reason", reason);
        }
    }

    record ProductAlreadyExisted(String name) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "cat.service.product.product_already_existed";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("name", name);
        }
    }

    record EmptyVariantOverrides(String intent) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "cat.service.variant.empty_overrides";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("intent", intent);
        }
    }

    record InvalidEnumValue(String enumName, String value) implements CatalogServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "cat.service.invalid_enum_value";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("enumName", enumName, "value", value);
        }
    }
}
