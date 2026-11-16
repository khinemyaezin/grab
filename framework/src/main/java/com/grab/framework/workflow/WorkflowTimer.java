package com.grab.framework.workflow;

import java.time.Instant;
import java.util.Objects;

public record WorkflowTimer(
        String nodeId,
        String logicalOperationId,
        TimerPurpose purpose,
        int generation,
        Instant dueAt,
        WorkflowPhase phase
) {

    public WorkflowTimer {
        nodeId = requireText(nodeId, "nodeId");
        logicalOperationId = requireText(logicalOperationId, "logicalOperationId");
        purpose = Objects.requireNonNull(purpose, "purpose");
        if (generation < 1) {
            throw new IllegalArgumentException("generation must be positive");
        }
        dueAt = Objects.requireNonNull(dueAt, "dueAt");
        phase = Objects.requireNonNull(phase, "phase");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

}
