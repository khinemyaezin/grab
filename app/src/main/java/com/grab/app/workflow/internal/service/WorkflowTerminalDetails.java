package com.grab.app.workflow.internal.service;

public record WorkflowTerminalDetails(
        String createdBy,
        String scopeId,
        boolean partiallyApplied
) {
}

