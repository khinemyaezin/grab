package com.grab.framework.workflow;

public interface WorkflowStep {

    String name();

    Object execute(WorkflowContext context);

    default void compensate(WorkflowContext context, Object output) {
    }
}
