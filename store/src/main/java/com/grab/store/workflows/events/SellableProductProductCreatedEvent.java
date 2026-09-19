package com.grab.store.workflows.events;

import com.grab.framework.workflow.SignalType;
import com.grab.framework.workflow.WorkflowSignalEvent;

import java.time.Instant;
import java.util.List;

public record SellableProductProductCreatedEvent(
        String workflowId,
        String productId,
        List<String> skus,
        List<VariantRef> variants,
        String status,
        Instant occurredAt,
        int version
) implements WorkflowSignalEvent {

    public SellableProductProductCreatedEvent {
        skus = skus == null ? List.of() : List.copyOf(skus);
        variants = variants == null ? List.of() : List.copyOf(variants);
    }

    public SellableProductProductCreatedEvent(
            String workflowId,
            String productId,
            List<String> skus,
            List<VariantRef> variants,
            Instant occurredAt,
            int version
    ) {
        this(workflowId, productId, skus, variants, null, occurredAt, version);
    }

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
        return "product-created:" + productId;
    }

    public record VariantRef(String variantId, String sku) {
    }
}
