package com.catalog.domain.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface CatalogDomainError extends MessageSource permits
        CatalogDomainError.InvalidProductStatusTransition,
        CatalogDomainError.ProductActivationRequiresActiveVariants,
        CatalogDomainError.TooManyVariantCombinations {

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
}
