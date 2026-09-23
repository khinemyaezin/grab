package com.customer.adapter.persistence.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface CustomerInfraError extends MessageSource permits
        CustomerInfraError.PersistenceConflict,
        CustomerInfraError.PersistenceInternal {

    record PersistenceConflict(String resource) implements CustomerInfraError {
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        public String code() {
            return "customer.infra.persistence.conflict";
        }

        public Map<String, Object> args() {
            return Map.of("resource", resource);
        }
    }

    record PersistenceInternal(String resource) implements CustomerInfraError {
        public ErrorCategory kind() {
            return ErrorCategory.INTERNAL;
        }

        public String code() {
            return "customer.infra.persistence.internal";
        }

        public Map<String, Object> args() {
            return Map.of("resource", resource);
        }
    }
}
