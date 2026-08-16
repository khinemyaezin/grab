package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.inventory.internal.command.AdjustStockCommand;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.UpdateReorderConfigCommand;
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.RequestSyncInventoryItemEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.inventory.domain.enums.AdjustmentReason;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UpdateSellableProductInventoryEventListener {

    private static final Logger log = Loggers.getLogger(UpdateSellableProductInventoryEventListener.class);
    private static final int EVENT_VERSION = 1;
    private static final String STEP_SYNC_INVENTORY_ITEM = "sync-inventory-item";

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final ApplicationEventPublisher events;

    @EventListener
    public void onRequestSyncInventoryItem(RequestSyncInventoryItemEvent event) {
        log.info(
                "Handling RequestSyncInventoryItemEvent workflowId={} sku={} locationId={}",
                event.workflowId(),
                event.sku(),
                event.locationId()
        );
        try {
            boolean created = event.inventoryItemId() == null || event.inventoryItemId().isBlank();
            InventoryItemResult result;
            if (created) {
                CreateInventoryCommand command = new CreateInventoryCommand(
                        event.sku(),
                        idGenerator.convertIdFrom(event.merchantId()),
                        idGenerator.convertIdFrom(event.locationId()),
                        event.onHandQuantity(),
                        event.safetyStock(),
                        event.reorderPoint(),
                        event.reorderQuantity(),
                        event.maxStock(),
                        idGenerator.convertIdFrom(event.createdBy()),
                        event.scopeKey(),
                        event.scopeId()
                );
                result = commandBus.dispatch(command);
            } else {
                Id inventoryItemId = idGenerator.convertIdFrom(event.inventoryItemId());
                Id createdBy = idGenerator.convertIdFrom(event.createdBy());
                result = commandBus.dispatch(new AdjustStockCommand(
                        inventoryItemId,
                        event.onHandQuantity(),
                        AdjustmentReason.CORRECTION,
                        createdBy,
                        event.scopeKey(),
                        event.scopeId()
                ));
                result = commandBus.dispatch(new UpdateReorderConfigCommand(
                        inventoryItemId,
                        defaultInt(event.safetyStock()),
                        defaultInt(event.reorderPoint()),
                        defaultInt(event.reorderQuantity()),
                        event.maxStock(),
                        createdBy,
                        event.scopeKey(),
                        event.scopeId()
                ));
            }
            events.publishEvent(new InventoryItemSyncedEvent(
                    event.workflowId(),
                    result.id(),
                    result.sku(),
                    result.locationId(),
                    created,
                    Instant.now(),
                    EVENT_VERSION
            ));
        } catch (RuntimeException exception) {
            log.warn(
                    "Sync inventory failed for workflowId={} sku={}: {}",
                    event.workflowId(),
                    event.sku(),
                    exception.getMessage()
            );
            events.publishEvent(new SellableProductStepFailedEvent(
                    event.workflowId(),
                    STEP_SYNC_INVENTORY_ITEM,
                    exception.getMessage(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
