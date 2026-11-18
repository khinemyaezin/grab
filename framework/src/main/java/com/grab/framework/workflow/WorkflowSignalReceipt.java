package com.grab.framework.workflow;

import java.time.Instant;
import java.util.Objects;

public record WorkflowSignalReceipt(
        String messageId,
        String executionId,
        String nodeId,
        String logicalOperationId,
        String attemptId,
        int attempt,
        WorkflowPhase phase,
        String payloadFingerprint,
        String outcomeFingerprint,
        int fingerprintVersion,
        int payloadVersion,
        WorkflowSignalOutcome outcome,
        WorkflowSignalReceiptStatus status,
        Instant receivedAt,
        Instant processedAt
) {

    public WorkflowSignalReceipt {
        messageId = requireText(messageId, "messageId");
        executionId = requireText(executionId, "executionId");
        nodeId = requireText(nodeId, "nodeId");
        logicalOperationId = requireText(logicalOperationId, "logicalOperationId");
        attemptId = requireText(attemptId, "attemptId");
        if (attempt < 1) {
            throw new IllegalArgumentException("attempt must be positive");
        }
        phase = Objects.requireNonNull(phase, "phase");
        payloadFingerprint = requireText(payloadFingerprint, "payloadFingerprint");
        if (fingerprintVersion < 1) {
            throw new IllegalArgumentException("fingerprintVersion must be positive");
        }
        if (payloadVersion < 1) {
            throw new IllegalArgumentException("payloadVersion must be positive");
        }
        outcome = Objects.requireNonNull(outcome, "outcome");
        status = Objects.requireNonNull(status, "status");
        receivedAt = Objects.requireNonNull(receivedAt, "receivedAt");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
