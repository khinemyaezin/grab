package com.grab.store.catalog.internal.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface CatalogCommandHandlerError extends MessageSource permits
    CatalogCommandHandlerError.VariantOverrideCombinationNotFound{

    record VariantOverrideCombinationNotFound(String sku) implements CatalogCommandHandlerError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "cat.service.product.variant_override.combination_not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("sku", sku);
        }
    }
}
