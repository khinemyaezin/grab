package com.grab.app.ordermanagement.workflows.internal.service;

public record WorkflowTerminalDetails(
        String createdBy,
        String scopeId,
        boolean partiallyApplied
) {
}
