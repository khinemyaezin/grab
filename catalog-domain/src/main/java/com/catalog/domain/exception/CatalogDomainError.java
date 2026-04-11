package com.catalog.domain.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface CatalogDomainError extends MessageSource permits
        CatalogDomainError.InvalidProductStatusTransition,
        CatalogDomainError.ProductActivationRequiresActiveVariants,
        CatalogDomainError.CannotDeleteLastActiveVariantFromActiveProduct,
        CatalogDomainError.TooManyVariantCombinations,
        CatalogDomainError.ListingIncomplete,
        CatalogDomainError.C2CConditionRequired,
        CatalogDomainError.OfferEligibilityOnlyForC2C,
        CatalogDomainError.InvalidVariantTypeName,
        CatalogDomainError.InvalidVariantOptionName,
        CatalogDomainError.DuplicateVariantOption,
        CatalogDomainError.VariantOptionTypeMismatch,
        CatalogDomainError.DuplicateCategoryVariantTypeLink {

    record InvalidProductStatusTransition(String currentStatus, String newStatus) implements CatalogDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.domain.invalid_product_status_transition";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "currentStatus", currentStatus,
                    "newStatus", newStatus
            );
        }
    }

    record ProductActivationRequiresActiveVariants() implements CatalogDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.domain.product_activation_requires_active_variants";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record CannotDeleteLastActiveVariantFromActiveProduct(String variantId) implements CatalogDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.domain.cannot_delete_last_active_variant_from_active_product";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("variantId", variantId);
        }
    }

    record TooManyVariantCombinations(int totalCombinations, int maxAllowed) implements CatalogDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.domain.too_many_variant_combinations";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "totalCombinations", totalCombinations,
                    "maxAllowed", maxAllowed
            );
        }
    }

    record ListingIncomplete() implements CatalogDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.domain.listing_incomplete";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record C2CConditionRequired() implements CatalogDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.domain.c2c_condition_required";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record OfferEligibilityOnlyForC2C() implements CatalogDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.domain.offer_eligibility_only_for_c2c";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record InvalidVariantTypeName(String typeId) implements CatalogDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "cat.domain.invalid_variant_type_name";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("typeId", typeId);
        }
    }

    record InvalidVariantOptionName(String typeId, String optionId) implements CatalogDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "cat.domain.invalid_variant_option_name";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "typeId", typeId,
                    "optionId", optionId
            );
        }
    }

    record DuplicateVariantOption(String optionName, String typeName) implements CatalogDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.domain.duplicate_variant_option_id";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "optionName", optionName,
                    "typeName", typeName
            );
        }
    }

    record VariantOptionTypeMismatch(String typeId, String optionId) implements CatalogDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.domain.variant_option_type_mismatch";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "typeId", typeId,
                    "optionId", optionId
            );
        }
    }

    record DuplicateCategoryVariantTypeLink(String categoryId, String typeId) implements CatalogDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "cat.domain.duplicate_category_variant_type_link";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "categoryId", categoryId,
                    "typeId", typeId
            );
        }
    }
}
