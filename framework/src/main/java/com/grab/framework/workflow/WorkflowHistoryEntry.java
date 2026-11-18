package com.grab.framework.workflow;

import java.time.Instant;
import java.util.Objects;

public record WorkflowHistoryEntry(
        String executionId,
        long sequenceNumber,
        String nodeId,
        String eventType,
        String detailsJson,
        Instant occurredAt
) {

    public WorkflowHistoryEntry {
        executionId = requireText(executionId, "executionId");
        if (sequenceNumber < 1) {
            throw new IllegalArgumentException("sequenceNumber must be positive");
        }
        eventType = requireText(eventType, "eventType");
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
