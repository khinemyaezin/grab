package com.grab.store.workflows.internal.updateproductvariant.event;

import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import com.grab.store.workflows.events.VariantUpdatedEvent;
import com.grab.store.workflows.internal.updateproductvariant.UpdateProductVariantOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateProductVariantWorkflowEventListener {

    private final UpdateProductVariantOrchestrator orchestrator;

    @EventListener
    public void onVariantUpdated(VariantUpdatedEvent event) {
        orchestrator.onVariantUpdated(event);
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
