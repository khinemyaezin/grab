package com.grab.framework.workflow;

public interface WorkflowStepFailure {

    String workflowId();

    String step();

    String message();
}
