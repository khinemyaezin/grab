package com.catalog.infrastructure.exception;

import com.grab.framework.exception.DomainException;
import com.grab.framework.service.MessageSource;

import java.util.Map;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException() {
        super(new ResourceNotFoundError(), "Resource not found.");
    }

    record ResourceNotFoundError() implements MessageSource {
        private static final String CODE = "exception.product.infrastructure.resource_not_found_error";
        @Override
        public String code() {
            return CODE;
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }
}
