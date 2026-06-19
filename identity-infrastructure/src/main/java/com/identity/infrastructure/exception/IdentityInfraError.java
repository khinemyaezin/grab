package com.identity.infrastructure.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface IdentityInfraError extends MessageSource permits
        IdentityInfraError.PersistenceConflict,
        IdentityInfraError.PersistenceInternal {

    record PersistenceConflict(String resource, String reason) implements IdentityInfraError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "identity.infra.persistence.conflict";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "resource", resource,
                    "reason", reason
            );
        }
    }

    record PersistenceInternal(String resource, String reason) implements IdentityInfraError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.INTERNAL;
        }

        @Override
        public String code() {
            return "identity.infra.persistence.internal";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "resource", resource,
                    "reason", reason
            );
        }
    }
}
