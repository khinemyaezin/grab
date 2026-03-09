package com.catalog.infrastructure.exception;

import com.grab.framework.exception.DomainException;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException() {
        super(new ResourceNotFoundError(), "Resource not found.");
    }

    record ResourceNotFoundError() implements MessageSource {
        private static final String CODE = "exception.catalog.infrastructure.resource_not_found_error";
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

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
