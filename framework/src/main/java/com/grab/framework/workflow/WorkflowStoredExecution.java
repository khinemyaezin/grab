package com.grab.framework.workflow;

import java.time.Instant;
import java.util.Objects;

public record WorkflowStoredExecution(
        String callerScope,
        int payloadVersion,
        String idempotencyKey,
        WorkflowExecutionState state,
        Instant createdAt,
        Instant updatedAt,
        long version
) {

    public WorkflowStoredExecution {
        callerScope = requireText(callerScope, "callerScope");
        if (payloadVersion < 1) {
            throw new IllegalArgumentException("payloadVersion must be positive");
        }
        idempotencyKey = requireText(idempotencyKey, "idempotencyKey");
        state = Objects.requireNonNull(state, "state");
        createdAt = Objects.requireNonNull(createdAt, "createdAt");
        updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative");
        }
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
