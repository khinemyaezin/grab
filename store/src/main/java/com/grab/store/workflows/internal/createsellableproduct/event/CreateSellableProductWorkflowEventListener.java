package com.grab.store.workflows.internal.createsellableproduct.event;

import com.grab.store.workflows.internal.createsellableproduct.CreateSellableProductOrchestrator;
import com.grab.store.workflows.events.InventoryItemCreatedEvent;
import com.grab.store.workflows.events.ProductVariantViewProjectedEvent;
import com.grab.store.workflows.events.SellableProductProductCreatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateSellableProductWorkflowEventListener {

    private final CreateSellableProductOrchestrator orchestrator;

    @EventListener
    public void onProductCreated(SellableProductProductCreatedEvent event) {
        orchestrator.onProductCreated(event);
    }

    @EventListener
    public void onProductViewProjected(ProductVariantViewProjectedEvent event) {
        orchestrator.onProductViewProjected(event);
    }

    @EventListener
    public void onInventoryItemCreated(InventoryItemCreatedEvent event) {
        orchestrator.onInventoryItemCreated(event);
    }

    @EventListener
    public void onStepFailed(SellableProductStepFailedEvent event) {
        orchestrator.onStepFailed(event);
    }
}
