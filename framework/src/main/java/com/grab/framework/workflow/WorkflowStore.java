package com.grab.framework.workflow;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WorkflowStore {

    WorkflowInstance save(WorkflowInstance instance);

    Optional<WorkflowInstance> findById(String workflowId);

    Optional<WorkflowInstance> findByIdempotencyKey(String workflowName, String idempotencyKey);

    List<WorkflowInstance> findByWorkflowNameAndStatus(String workflowName, WorkflowStatus status);

    Optional<WorkflowInstance> findByCorrelationKey(String workflowName, String correlationKey);

    List<WorkflowInstance> findAllByCorrelationKey(String correlationKey);

    void putCorrelation(String workflowName, String correlationKey, String workflowId, String step);

    void removeCorrelationsForWorkflow(String workflowId);

    boolean tryRecordSignal(String workflowId, String step, String dedupKey);

    List<WorkflowInstance> findResumable(
            String workflowName,
            Collection<WorkflowStatus> statuses,
            Instant updatedBefore,
            int limit
    );
}
