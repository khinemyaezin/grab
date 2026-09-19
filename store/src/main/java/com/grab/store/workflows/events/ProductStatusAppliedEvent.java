package com.grab.store.workflows.events;

import com.grab.framework.workflow.SignalType;
import com.grab.framework.workflow.WorkflowSignalEvent;

import java.time.Instant;

public record ProductStatusAppliedEvent(
        String workflowId,
        String productId,
        String status,
        Instant occurredAt,
        int version
) implements WorkflowSignalEvent {

    @Override
    public SignalType signalType() {
        return SignalType.COMPLETION;
    }

    @Override
    public String signalWorkflowId() {
        return workflowId;
    }

    @Override
    public String signalDedupKey() {
        return "product-status-applied:" + productId + ":" + status;
    }
}
