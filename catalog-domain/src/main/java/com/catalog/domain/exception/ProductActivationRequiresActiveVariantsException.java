package com.catalog.domain.exception;

import com.grab.framework.exception.DomainException;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public class ProductActivationRequiresActiveVariantsException extends DomainException {
    public ProductActivationRequiresActiveVariantsException() {
        super(
                new ProductActivationRequiresActiveVariantsError(),
                "Cannot activate product without active variants.");
    }
    record ProductActivationRequiresActiveVariantsError() implements MessageSource {
        private static final String CODE = "exception.catalog.domain.product_activation_requires_active_variants_error";
        @Override
        public String code() {
            return CODE;
        }

        @Override
        public Map<String, Object> args() {
            return Map.of( );
        }
    }
}
