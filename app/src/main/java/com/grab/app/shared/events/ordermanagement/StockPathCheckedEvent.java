package com.grab.app.shared.events.ordermanagement;

import com.grab.framework.workflow.SignalType;
import com.grab.framework.workflow.WorkflowSignalEvent;

import java.time.Instant;

public record StockPathCheckedEvent(
        String workflowId,
        String salesChannelId,
        boolean missingRoute,
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
        return "stock-path-checked:" + salesChannelId;
    }
}
