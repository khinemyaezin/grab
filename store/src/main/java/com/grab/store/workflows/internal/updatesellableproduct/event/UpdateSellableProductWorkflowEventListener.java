package com.grab.store.workflows.internal.updatesellableproduct.event;

import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.ProductVariantViewProjectedEvent;
import com.grab.store.workflows.events.SellableProductProductUpdatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import com.grab.store.workflows.internal.updatesellableproduct.UpdateSellableProductOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateSellableProductWorkflowEventListener {

    private final UpdateSellableProductOrchestrator orchestrator;

    @EventListener
    public void onProductUpdated(SellableProductProductUpdatedEvent event) {
        orchestrator.onProductUpdated(event);
    }

    @EventListener
    public void onProductViewProjected(ProductVariantViewProjectedEvent event) {
        orchestrator.onProductViewProjected(event);
    }

    @EventListener
    public void onVariantPriceSynced(VariantPriceSyncedEvent event) {
        orchestrator.onVariantPriceSynced(event);
    }

    @EventListener
    public void onInventoryItemSynced(InventoryItemSyncedEvent event) {
        orchestrator.onInventoryItemSynced(event);
    }

    @EventListener
    public void onStepFailed(SellableProductStepFailedEvent event) {
        orchestrator.onStepFailed(event);
    }
}
