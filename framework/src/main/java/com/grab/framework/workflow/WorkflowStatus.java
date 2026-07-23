package com.grab.framework.workflow;

public enum WorkflowStatus {
    RUNNING,
    WAITING_EXTERNAL,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}
