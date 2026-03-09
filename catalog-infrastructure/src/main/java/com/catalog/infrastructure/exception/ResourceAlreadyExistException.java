package com.catalog.infrastructure.exception;

import com.grab.framework.exception.DomainException;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public class ResourceAlreadyExistException extends DomainException {
    public ResourceAlreadyExistException() {
        super(new ResourceAlreadyExistError(), "Resource already exists.");
    }
    record ResourceAlreadyExistError() implements MessageSource {
        private static final String CODE = "exception.catalog.infrastructure.resource_already_exist_error";
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
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
