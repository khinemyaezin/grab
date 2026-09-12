package com.grab.framework.workflow;

/**
 * A Spring-discovered workflow. Implementations are beans; {@code WorkflowDefinitionRegistry}
 * collects them the same way the command bus collects {@code CommandHandler}s.
 */
public interface WorkflowProcess<C> {

    ProcessDefinition<C> create();
}