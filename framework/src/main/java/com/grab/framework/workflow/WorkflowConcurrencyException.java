package com.grab.framework.workflow;

public final class WorkflowConcurrencyException extends RuntimeException {

    public WorkflowConcurrencyException(String executionId, Throwable cause) {
        super("Concurrent workflow update: " + executionId, cause);
    }
}
