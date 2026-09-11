package com.grab.framework.workflow;

public final class WorkflowOptimisticLockException extends RuntimeException {

    public WorkflowOptimisticLockException(String workflowId) {
        super("Optimistic lock failed for workflow " + workflowId);
    }
}
