package com.grab.framework.workflow;

import java.time.Instant;
import java.util.Objects;

public record WorkflowExecutionMetadata(
        String executionId,
        String definitionName,
        int definitionVersion,
        int payloadVersion,
        String callerScope,
        ExecutionStatus status,
        boolean cancellationRequested,
        Instant createdAt,
        Instant updatedAt
) {

    public WorkflowExecutionMetadata {
        executionId = requireText(executionId, "executionId");
        definitionName = requireText(definitionName, "definitionName");
        if (definitionVersion < 1) {
            throw new IllegalArgumentException("definitionVersion must be positive");
        }
        if (payloadVersion < 1) {
            throw new IllegalArgumentException("payloadVersion must be positive");
        }
        callerScope = requireText(callerScope, "callerScope");
        status = Objects.requireNonNull(status, "status");
        createdAt = Objects.requireNonNull(createdAt, "createdAt");
        updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
