package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.domain.Event;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.application.model.write.CreateInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.grab.store.workflows.events.InventoryItemCreatedEvent;
import com.grab.store.workflows.events.RequestCreateInventoryItemEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.inventory.adapter.persistence.workflow.InventoryWorkflowStepRunner;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@AllArgsConstructor
public class CreateSellableProductInventoryEventListener {

    private static final Logger log = Loggers.getLogger(CreateSellableProductInventoryEventListener.class);
    private static final int EVENT_VERSION = 1;
    private static final String STEP_CREATE_INVENTORY_ITEM = "create-inventory-item";

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final InventoryWorkflowStepRunner signalEmitter;

    @EventListener
    public void onRequestCreateInventoryItem(RequestCreateInventoryItemEvent event) {
        log.info(
                "Handling RequestCreateInventoryItemEvent workflowId={} sku={} locationId={}",
                event.workflowId(),
                event.sku(),
                event.locationId()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> createInventoryItem(event),
                exception -> {
                    log.warn(
                            "Create inventory failed for workflowId={} sku={}: {}",
                            event.workflowId(),
                            event.sku(),
                            exception.getMessage()
                    );
                    return List.of(new SellableProductStepFailedEvent(
                            event.workflowId(),
                            STEP_CREATE_INVENTORY_ITEM,
                            exception.getMessage(),
                            Instant.now(),
                            EVENT_VERSION
                    ));
                }
        );
    }

    private List<Event> createInventoryItem(RequestCreateInventoryItemEvent event) {
        CreateInventoryCommand command = new CreateInventoryCommand(
                event.sku(),
                event.variantId(),
                idGenerator.convertIdFrom(event.merchantId()),
                idGenerator.convertIdFrom(event.locationId()),
                event.initialQuantity(),
                event.safetyStock(),
                event.reorderPoint(),
                event.reorderQuantity(),
                event.maxStock(),
                idGenerator.convertIdFrom(event.createdBy()),
                event.scopeKey(),
                event.scopeId()
        );
        InventoryItemResult result = commandBus.dispatch(command);
        return List.of(new InventoryItemCreatedEvent(
                event.workflowId(),
                result.id(),
                result.sku(),
                result.locationId(),
                Instant.now(),
                EVENT_VERSION
        ));
    }
}
