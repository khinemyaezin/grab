package com.grab.framework.workflow;

import java.util.Optional;

public interface WorkflowStore {

    WorkflowInstance save(WorkflowInstance instance);

    Optional<WorkflowInstance> findById(String workflowId);

    Optional<WorkflowInstance> findByIdempotencyKey(String workflowName, String idempotencyKey);
}
