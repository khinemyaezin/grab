package com.grab.store.workflows.events;

import com.grab.framework.workflow.CorrelationKey;
import com.grab.framework.workflow.SignalType;
import com.grab.framework.workflow.WorkflowSignalEvent;

import java.time.Instant;

/**
 * Emitted by the inventory projection after a product-variant view is written.
 *
 * <p>This is inventory-internal choreography. Sellable-product workflows do not wait on it;
 * catalog completion already returns {@code variantId} for inventory CREATE.
 * The event carries no workflow id because the projection reacts to catalog integration events.
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
