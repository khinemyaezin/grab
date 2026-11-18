package com.grab.framework.workflow;

import com.grab.framework.domain.Event;

import java.util.Objects;

public record WorkflowTaskDispatchEvent(
        String callerScope,
        String definitionName,
        int definitionVersion,
        int payloadVersion,
        String executionId,
        String nodeId,
        String operation,
        String logicalOperationId,
        String attemptId,
        int attempt,
        WorkflowPhase phase,
        Object input
) implements Event {

    public WorkflowTaskDispatchEvent {
        callerScope = requireText(callerScope, "callerScope");
        definitionName = requireText(definitionName, "definitionName");
        if (definitionVersion < 1 || payloadVersion < 1) {
            throw new IllegalArgumentException("definition and payload versions must be positive");
        }
        executionId = requireText(executionId, "executionId");
        nodeId = requireText(nodeId, "nodeId");
        operation = requireText(operation, "operation");
        logicalOperationId = requireText(logicalOperationId, "logicalOperationId");
        attemptId = requireText(attemptId, "attemptId");
        if (attempt < 1) {
            throw new IllegalArgumentException("attempt must be positive");
        }
        phase = Objects.requireNonNull(phase, "phase");
        input = Objects.requireNonNull(input, "input");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
