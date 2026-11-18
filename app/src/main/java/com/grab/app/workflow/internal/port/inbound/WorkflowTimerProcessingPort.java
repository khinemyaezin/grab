package com.grab.app.workflow.internal.port.inbound;

import com.grab.framework.workflow.WorkflowTimerClaim;

import java.util.Optional;

public interface WorkflowTimerProcessingPort {

    Optional<WorkflowTimerClaim> claim();

    void process(WorkflowTimerClaim claim);
}
