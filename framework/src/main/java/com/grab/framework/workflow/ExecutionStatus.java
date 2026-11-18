package com.grab.framework.workflow;

public enum ExecutionStatus {
    RUNNING,
    WAITING,
    COMPENSATING,
    COMPLETED,
    FAILED,
    CANCEL_REQUESTED,
    CANCELLED,
    COMPENSATED,
    NEEDS_ATTENTION
}
