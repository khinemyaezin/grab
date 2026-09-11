package com.grab.framework.workflow;

public interface WorkflowLifecycleListener {

    default void onTerminal(WorkflowInstance instance, Object context) {
    }
}
