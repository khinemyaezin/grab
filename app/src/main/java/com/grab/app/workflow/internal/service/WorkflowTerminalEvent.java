package com.grab.app.workflow.internal.service;

import java.time.Instant;

public record WorkflowTerminalEvent(
        String createdBy,
        String scopeId,
        String workflowId,
        String workflowName,
        String status,
        String idempotencyKey,
        String errorMessage,
        boolean partiallyApplied,
        Instant occurredAt
) {
}

