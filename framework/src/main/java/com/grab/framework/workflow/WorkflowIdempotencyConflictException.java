package com.grab.framework.workflow;

public final class WorkflowIdempotencyConflictException extends IllegalStateException {

    public WorkflowIdempotencyConflictException(String message) {
        super(message);
    }
}
