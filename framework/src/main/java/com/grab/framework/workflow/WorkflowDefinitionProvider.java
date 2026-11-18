package com.grab.framework.workflow;

public interface WorkflowDefinitionProvider<I, O> {

    WorkflowIdentity identity();

    WorkflowDefinition<I, O> definition();

    void validateStart(String callerScope, I input);
}
