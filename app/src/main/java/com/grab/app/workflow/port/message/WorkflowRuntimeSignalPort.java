package com.grab.app.workflow.port.message;

import com.grab.framework.id.Id;
import com.grab.framework.workflow.WorkflowPhase;
import com.grab.framework.workflow.WorkflowFailureClassification;

import java.time.Instant;
import java.util.Objects;

public interface WorkflowRuntimeSignalPort {

    SignalResult accept(SignalMessage message);

    record SignalMessage(
            String messageId,
            Id executionId,
            String nodeId,
            String logicalOperationId,
            String attemptId,
            int attempt,
            WorkflowPhase phase,
            SignalOutcome outcome,
            String payloadJson,
            int payloadVersion,
            String callerScope,
            WorkflowFailureClassification failureClassification,
            String errorCode,
            String errorMessage,
            Instant occurredAt
    ) {

        public SignalMessage {
            messageId = requireText(messageId, "messageId");
            executionId = Objects.requireNonNull(executionId, "executionId");
            nodeId = requireText(nodeId, "nodeId");
            logicalOperationId = requireText(logicalOperationId, "logicalOperationId");
            attemptId = requireText(attemptId, "attemptId");
            if (attempt < 1 || payloadVersion < 1) {
                throw new IllegalArgumentException("attempt and payloadVersion must be positive");
            }
            phase = Objects.requireNonNull(phase, "phase");
            outcome = Objects.requireNonNull(outcome, "outcome");
            callerScope = requireText(callerScope, "callerScope");
            failureClassification = Objects.requireNonNull(failureClassification, "failureClassification");
            if (outcome != SignalOutcome.FAILED
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

    enum SignalOutcome {
        SUCCEEDED,
        FAILED,
        UNKNOWN
    }

    record SignalResult(Id executionId, String status, boolean duplicate) {
    }
}
