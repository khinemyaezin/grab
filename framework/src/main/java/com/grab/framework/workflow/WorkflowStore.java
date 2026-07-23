package com.grab.framework.workflow;

import java.util.List;
import java.util.Optional;

public interface WorkflowStore {

    WorkflowInstance save(WorkflowInstance instance);

    Optional<WorkflowInstance> findById(String workflowId);

    Optional<WorkflowInstance> findByIdempotencyKey(String workflowName, String idempotencyKey);

    List<WorkflowInstance> findByWorkflowNameAndStatus(String workflowName, WorkflowStatus status);
}
