package com.grab.store.workflows.events;

import com.grab.framework.workflow.SignalType;
import com.grab.framework.workflow.WorkflowSignalEvent;
import com.grab.framework.workflow.WorkflowStepFailure;

import java.time.Instant;

public record PublishProductStepFailedEvent(
        String workflowId,
        String step,
        String message,
        Instant occurredAt,
        int version
) implements WorkflowSignalEvent, WorkflowStepFailure {

    @Override
    public SignalType signalType() {
        return SignalType.FAILURE;
    }

    @Override
    public String signalWorkflowId() {
        return workflowId;
    }

    @Override
    public String signalDedupKey() {
        return "failed:" + step + ":" + message;
    }
}
