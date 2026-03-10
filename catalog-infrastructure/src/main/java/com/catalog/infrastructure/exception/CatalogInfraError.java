package com.catalog.infrastructure.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface CatalogInfraError extends MessageSource permits
        CatalogInfraError.PersistenceConflict,
        CatalogInfraError.PersistenceNotFound,
        CatalogInfraError.PersistenceInternal {

    record PersistenceConflict(String resource, String reason) implements CatalogInfraError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "cat.infra.persistence.conflict";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "resource", resource,
                    "reason", reason
            );
        }
    }

    record PersistenceNotFound(String resource, String id) implements CatalogInfraError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "cat.infra.persistence.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "resource", resource,
                    "id", id
            );
        }
    }

    record PersistenceInternal(String resource, String reason) implements CatalogInfraError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.INTERNAL;
        }

        @Override
        public String code() {
            return "cat.infra.persistence.internal";
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
