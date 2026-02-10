package com.product.infrastructure.exception;

import com.grab.framework.exception.DomainException;
import com.grab.framework.service.MessageSource;

import java.util.Map;

public class ResourceAlreadyExistException extends DomainException {
    public ResourceAlreadyExistException() {
        super(new ResourceAlreadyExistError(), "Resource already exists.");
    }
    record ResourceAlreadyExistError() implements MessageSource {
        private static final String CODE = "exception.product.infrastructure.resource_already_exist_error";
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
