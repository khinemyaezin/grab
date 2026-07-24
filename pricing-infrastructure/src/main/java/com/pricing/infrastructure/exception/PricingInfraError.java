package com.pricing.infrastructure.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface PricingInfraError extends MessageSource permits
        PricingInfraError.PersistenceConflict,
        PricingInfraError.PersistenceInternal {

    record PersistenceConflict(String resource) implements PricingInfraError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "pri.infra.persistence.conflict";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("resource", resource);
        }
    }

    record PersistenceInternal(String resource) implements PricingInfraError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.INTERNAL;
        }

        @Override
        public String code() {
            return "pri.infra.persistence.internal";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("resource", resource);
        }
    }
}
