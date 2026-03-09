package com.inventory.infrastructure.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface InventoryInfraError extends MessageSource permits
        InventoryInfraError.PersistenceConflict,
        InventoryInfraError.PersistenceNotFound,
        InventoryInfraError.PersistenceInternal {

    record PersistenceConflict(String resource, String reason) implements InventoryInfraError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "inv.infra.persistence.conflict";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "resource", resource,
                    "reason", reason
            );
        }
    }

    record PersistenceNotFound(String resource, String id) implements InventoryInfraError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "inv.infra.persistence.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "resource", resource,
                    "id", id
            );
        }
    }

    record PersistenceInternal(String resource, String reason) implements InventoryInfraError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.INTERNAL;
        }

        @Override
        public String code() {
            return "inv.infra.persistence.internal";
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
