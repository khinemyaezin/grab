package com.cart.infrastructure.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface CartInfraError extends MessageSource permits
        CartInfraError.PersistenceConflict,
        CartInfraError.PersistenceInternal {

    record PersistenceConflict(String resource) implements CartInfraError {
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        public String code() {
            return "cart.infra.persistence.conflict";
        }

        public Map<String, Object> args() {
            return Map.of("resource", resource);
        }
    }

    record PersistenceInternal(String resource) implements CartInfraError {
        public ErrorCategory kind() {
            return ErrorCategory.INTERNAL;
        }

        public String code() {
            return "cart.infra.persistence.internal";
        }

        public Map<String, Object> args() {
            return Map.of("resource", resource);
        }
    }
}
