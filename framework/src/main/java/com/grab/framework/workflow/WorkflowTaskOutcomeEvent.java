package com.grab.framework.workflow;

import com.grab.framework.domain.Event;

import java.time.Instant;
import java.util.Objects;

public record WorkflowTaskOutcomeEvent(
        String messageId,
        String executionId,
        String nodeId,
        String logicalOperationId,
        String attemptId,
        int attempt,
        WorkflowPhase phase,
        WorkflowSignalOutcome outcome,
        String payloadJson,
        int payloadVersion,
        String callerScope,
        WorkflowFailureClassification failureClassification,
        String errorCode,
        String errorMessage,
        Instant occurredAt
) implements Event {

    public WorkflowTaskOutcomeEvent {
        messageId = requireText(messageId, "messageId");
        executionId = requireText(executionId, "executionId");
        nodeId = requireText(nodeId, "nodeId");
        logicalOperationId = requireText(logicalOperationId, "logicalOperationId");
        attemptId = requireText(attemptId, "attemptId");
        callerScope = requireText(callerScope, "callerScope");
        if (attempt < 1) {
            throw new IllegalArgumentException("attempt must be positive");
        }
        if (payloadVersion < 1) {
            throw new IllegalArgumentException("payloadVersion must be positive");
        }
        phase = Objects.requireNonNull(phase, "phase");
        outcome = Objects.requireNonNull(outcome, "outcome");
        failureClassification = Objects.requireNonNull(failureClassification, "failureClassification");
        if (outcome != WorkflowSignalOutcome.FAILED
                && failureClassification != WorkflowFailureClassification.UNCLASSIFIED) {
            throw new IllegalArgumentException("Only failed outcomes may carry a failure classification");
        }
        occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
