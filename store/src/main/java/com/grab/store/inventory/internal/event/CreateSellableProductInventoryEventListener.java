package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.workflows.events.InventoryItemCreatedEvent;
import com.grab.store.workflows.events.RequestCreateInventoryItemEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CreateSellableProductInventoryEventListener {

    private static final Logger log = Loggers.getLogger(CreateSellableProductInventoryEventListener.class);
    private static final int EVENT_VERSION = 1;
    private static final String STEP_CREATE_INVENTORY_ITEM = "create-inventory-item";

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final ApplicationEventPublisher events;

    @EventListener
    public void onRequestCreateInventoryItem(RequestCreateInventoryItemEvent event) {
        log.info(
                "Handling RequestCreateInventoryItemEvent workflowId={} sku={} locationId={}",
                event.workflowId(),
                event.sku(),
                event.locationId()
        );
        try {
            CreateInventoryCommand command = new CreateInventoryCommand(
                    event.sku(),
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
            events.publishEvent(new InventoryItemCreatedEvent(
                    event.workflowId(),
                    result.id(),
                    result.sku(),
                    result.locationId(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        } catch (RuntimeException exception) {
            log.warn(
                    "Create inventory failed for workflowId={} sku={}: {}",
                    event.workflowId(),
                    event.sku(),
                    exception.getMessage()
            );
            events.publishEvent(new SellableProductStepFailedEvent(
                    event.workflowId(),
                    STEP_CREATE_INVENTORY_ITEM,
                    exception.getMessage(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }
    }
}
