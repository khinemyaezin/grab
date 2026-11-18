package com.grab.framework.workflow;

import java.time.Instant;
import java.util.Objects;

public record WorkflowStartReceipt(
        String callerScope,
        String idempotencyKey,
        String definitionName,
        int definitionVersion,
        int fingerprintVersion,
        String inputFingerprint,
        String executionId,
        Instant createdAt
) {

    public WorkflowStartReceipt {
        callerScope = requireText(callerScope, "callerScope");
        idempotencyKey = requireText(idempotencyKey, "idempotencyKey");
        definitionName = requireText(definitionName, "definitionName");
        if (definitionVersion < 1) {
            throw new IllegalArgumentException("definitionVersion must be positive");
        }
        if (fingerprintVersion < 1) {
            throw new IllegalArgumentException("fingerprintVersion must be positive");
        }
        inputFingerprint = requireText(inputFingerprint, "inputFingerprint");
        executionId = requireText(executionId, "executionId");
        createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
