package com.grab.store.shared.sse;

public record WorkflowUiEnvelope(
        String producerId,
        String workflowId,
        String workflowName,
        String status,
        String productId,
        String errorMessage
) {
}
