package com.grab.store.shared.sse;

public record WorkflowTerminalUiEvent(
        String platformUserId,
        String scopeId,
        String workflowId,
        String workflowName,
        String status,
        String idempotencyKey,
        String errorMessage,
        boolean partiallyApplied
) {

    public String subscriberKey() {
        return SseSubscriberKey.of(platformUserId, scopeId);
    }

    public WorkflowUiEnvelope toEnvelope() {
        return new WorkflowUiEnvelope(
                "backend",
                workflowId,
                workflowName,
                status,
                idempotencyKey,
                errorMessage,
                partiallyApplied
        );
    }
}
