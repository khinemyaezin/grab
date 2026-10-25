package com.grab.app.shared.sse;

public record WorkflowUiEnvelope(
        String producerId,
        String workflowId,
        String workflowName,
        String status,
        String idempotencyKey,
        String errorMessage,
        boolean partiallyApplied
) {
}
