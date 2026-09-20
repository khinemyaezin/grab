package com.saleschannel.adapter.persistence.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface SalesChannelInfraError extends MessageSource permits
        SalesChannelInfraError.PersistenceConflict,
        SalesChannelInfraError.PersistenceInternal {

    record PersistenceConflict(String resource) implements SalesChannelInfraError {
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        public String code() {
            return "sc.infra.persistence.conflict";
        }

        public Map<String, Object> args() {
            return Map.of("resource", resource);
        }
    }

    record PersistenceInternal(String resource) implements SalesChannelInfraError {
        public ErrorCategory kind() {
            return ErrorCategory.INTERNAL;
        }

        public String code() {
            return "sc.infra.persistence.internal";
        }

        public Map<String, Object> args() {
            return Map.of("resource", resource);
        }
    }
}
