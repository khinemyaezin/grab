package com.grab.store.shared.sse;

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
