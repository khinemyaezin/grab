package com.grab.service.workflow.port.message;

import com.grab.framework.workflow.WorkflowTimerClaim;

import java.time.Instant;

public interface WorkflowTimerProcessingPort {

    void process(WorkflowTimerClaim claim, Instant processingTime);
}
