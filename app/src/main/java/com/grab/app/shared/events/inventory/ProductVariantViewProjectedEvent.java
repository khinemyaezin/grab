package com.grab.app.shared.events.inventory;

import com.grab.framework.workflow.CorrelationKey;
import com.grab.framework.workflow.SignalType;
import com.grab.framework.workflow.WorkflowSignalEvent;

import java.time.Instant;

public record ProductVariantViewProjectedEvent(
        String productId,
        String variantId,
        String sku,
        Instant occurredAt,
        int version
) implements WorkflowSignalEvent {

    @Override
    public SignalType signalType() {
        return SignalType.COMPLETION;
    }

    @Override
    public CorrelationKey signalCorrelation() {
        return productId == null || productId.isBlank() ? null : CorrelationKey.product(productId);
    }

    @Override
    public String signalDedupKey() {
        return "projected:" + productId + ":" + sku;
    }
}
