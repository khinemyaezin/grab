package com.grab.framework.workflow;

public enum WorkflowNodeKind {
    TASK,
    SEQUENCE,
    CHOICE,
    PARALLEL,
    WAIT_FOR_SIGNAL,
    DELAY,
    PASS_THROUGH,
    LOCAL_MAP,
    BOUND_TASK,
    JOINED_PARALLEL
}
