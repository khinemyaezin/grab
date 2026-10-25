package com.grab.app.shared.events.ordermanagement;

import com.grab.framework.workflow.SignalType;
import com.grab.framework.workflow.WorkflowSignalEvent;

import java.time.Instant;

public record ProductPublishedToChannelEvent(
        String workflowId,
        String productId,
        String variantId,
        String salesChannelId,
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
        return "product-published-to-channel:" + variantId + ":" + salesChannelId;
    }
}
