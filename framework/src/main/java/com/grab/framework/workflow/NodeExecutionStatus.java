package com.grab.framework.workflow;

public enum NodeExecutionStatus {
    PENDING,
    ACTIVE,
    DISPATCHED,
    RETRY_WAIT,
    SUCCEEDED,
    FAILED,
    SKIPPED,
    UNKNOWN,
    COMPENSATING,
    COMPENSATED
}
