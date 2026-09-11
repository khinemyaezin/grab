package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.inventory.internal.command.AdjustStockCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.RequestAdjustVariantStockEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UpdateProductVariantInventoryEventListener {

    private static final Logger log = Loggers.getLogger(UpdateProductVariantInventoryEventListener.class);
    private static final int EVENT_VERSION = 1;
    private static final String STEP_ADJUST_STOCK = "adjust-stock";

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final ApplicationEventPublisher events;

    @EventListener
    public void onRequestAdjustVariantStock(RequestAdjustVariantStockEvent event) {
        log.info(
                "Handling RequestAdjustVariantStockEvent workflowId={} inventoryItemId={}",
                event.workflowId(),
                event.inventoryItemId()
        );
        try {
            InventoryItemResult result = commandBus.dispatch(new AdjustStockCommand(
                    idGenerator.convertIdFrom(event.inventoryItemId()),
                    event.newOnHandQuantity(),
                    event.reason(),
                    idGenerator.convertIdFrom(event.createdBy()),
                    event.scopeKey(),
                    event.scopeId()
            ));
            events.publishEvent(new InventoryItemSyncedEvent(
                    event.workflowId(),
                    result.id(),
                    result.sku(),
                    result.locationId(),
                    false,
                    Instant.now(),
                    EVENT_VERSION
            ));
        } catch (RuntimeException exception) {
            log.warn(
                    "Adjust variant stock failed for workflowId={} inventoryItemId={}: {}",
                    event.workflowId(),
                    event.inventoryItemId(),
                    exception.getMessage()
            );
            events.publishEvent(new SellableProductStepFailedEvent(
                    event.workflowId(),
                    STEP_ADJUST_STOCK,
                    exception.getMessage(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }
    }
}
