package com.grab.framework.workflow;

import java.util.Optional;
import java.time.Instant;

public interface WorkflowExecutionStore {

    WorkflowStoredExecution create(WorkflowStoredExecution execution);

    WorkflowStoredExecution save(WorkflowStoredExecution execution);

    Optional<WorkflowStoredExecution> find(String executionId, WorkflowDefinition<?, ?> definition);

    boolean recordUnavailable(String executionId, String explanation, Instant now);

    Optional<WorkflowExecutionMetadata> findMetadata(String executionId);
}
