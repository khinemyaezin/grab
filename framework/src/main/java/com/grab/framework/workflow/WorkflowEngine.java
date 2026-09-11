package com.grab.framework.workflow;

import java.util.List;
import java.util.Optional;

public interface WorkflowEngine {

    <C> WorkflowInstance start(ProcessDefinition<C> definition, C input, String idempotencyKey);

    List<WorkflowInstance> onSignal(InboundSignal signal);

    Optional<WorkflowInstance> resume(String workflowId);

    void sweep();
}
