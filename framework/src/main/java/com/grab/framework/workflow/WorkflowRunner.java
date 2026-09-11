package com.grab.framework.workflow;

@Deprecated(since = "workflow-engine", forRemoval = true)
public interface WorkflowRunner {

    WorkflowResult run(WorkflowDefinition definition, WorkflowContext context, WorkflowRunRequest request);

    WorkflowResult resume(WorkflowDefinition definition, String workflowId);
}
