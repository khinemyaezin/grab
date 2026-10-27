package com.grab.app.workflows.internal.service;

public record WorkflowTerminalDetails(
        String createdBy,
        String scopeId,
        boolean partiallyApplied
) {
}
