package com.grab.store.workflows.internal.service;

public record WorkflowTerminalDetails(
        String createdBy,
        String scopeId,
        boolean partiallyApplied
) {
}
