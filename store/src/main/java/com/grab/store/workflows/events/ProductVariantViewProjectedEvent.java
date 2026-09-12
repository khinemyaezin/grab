package com.grab.store.workflows.events;

import com.grab.framework.workflow.CorrelationKey;
import com.grab.framework.workflow.SignalType;
import com.grab.framework.workflow.WorkflowSignalEvent;

import java.time.Instant;

/**
 * Emitted by the inventory projection after a product-variant view is written.
 *
 * <p>Carries no workflow id on purpose: the projection reacts to catalog integration events and has
 * no way to know which run, if any, is waiting. It routes by correlation on the product instead,
 * which also lets several runs waiting on the same product each receive it.
 */
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
