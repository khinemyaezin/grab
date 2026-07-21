package com.grab.framework.workflow;

public interface WorkflowRunner {

    WorkflowResult run(WorkflowDefinition definition, WorkflowContext context);
}
