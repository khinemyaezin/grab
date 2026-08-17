package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.inventory.internal.command.AdjustStockCommand;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.MarkDamagedCommand;
import com.grab.store.inventory.internal.command.UpdateReorderConfigCommand;
import com.grab.store.inventory.internal.command.WriteOffStockCommand;
import com.grab.store.workflows.events.InventoryItemSyncedEvent;
import com.grab.store.workflows.events.InventorySyncOp;
import com.grab.store.workflows.events.InventorySyncPayload;
import com.grab.store.workflows.events.RequestSyncInventoryItemEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
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
                "Handling RequestSyncInventoryItemEvent workflowId={} sku={} locationId={} op={}",
                event.workflowId(),
                event.sku(),
                event.locationId(),
                event.op()
        );
        try {
            boolean created = event.op() == InventorySyncOp.CREATE;
            InventoryItemResult result = dispatch(event);
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

    private InventoryItemResult dispatch(RequestSyncInventoryItemEvent event) {
        InventorySyncOp op = event.op();
        if (op == null) {
            throw new IllegalArgumentException("inventory sync op is required");
        }
        return switch (op) {
            case CREATE -> create(event);
            case ADJUST -> adjust(event);
            case DAMAGE -> damage(event);
            case WRITE_OFF -> writeOff(event);
            case REORDER -> reorderOnly(event);
        };
    }

    private InventoryItemResult create(RequestSyncInventoryItemEvent event) {
        InventorySyncPayload.CreateStock create = requirePayload(event.create(), "create");
        return commandBus.dispatch(new CreateInventoryCommand(
                event.sku(),
                idGenerator.convertIdFrom(event.merchantId()),
                idGenerator.convertIdFrom(event.locationId()),
                create.initialQuantity(),
                create.safetyStock(),
                create.reorderPoint(),
                create.reorderQuantity(),
                create.maxStock(),
                idGenerator.convertIdFrom(event.createdBy()),
                event.scopeKey(),
                event.scopeId()
        ));
    }

    private InventoryItemResult adjust(RequestSyncInventoryItemEvent event) {
        InventorySyncPayload.AdjustStock adjust = requirePayload(event.adjust(), "adjust");
        Id inventoryItemId = idGenerator.convertIdFrom(event.inventoryItemId());
        Id createdBy = idGenerator.convertIdFrom(event.createdBy());
        InventoryItemResult result = commandBus.dispatch(new AdjustStockCommand(
                inventoryItemId,
                adjust.newOnHandQuantity(),
                adjust.reason(),
                createdBy,
                event.scopeKey(),
                event.scopeId()
        ));
        return maybeReorder(event, inventoryItemId, createdBy, result);
    }

    private InventoryItemResult damage(RequestSyncInventoryItemEvent event) {
        InventorySyncPayload.DamageStock damage = requirePayload(event.damage(), "damage");
        Id inventoryItemId = idGenerator.convertIdFrom(event.inventoryItemId());
        Id createdBy = idGenerator.convertIdFrom(event.createdBy());
        InventoryItemResult result = commandBus.dispatch(new MarkDamagedCommand(
                inventoryItemId,
                damage.quantity(),
                damage.notes(),
                createdBy,
                event.scopeKey(),
                event.scopeId()
        ));
        return maybeReorder(event, inventoryItemId, createdBy, result);
    }

    private InventoryItemResult writeOff(RequestSyncInventoryItemEvent event) {
        InventorySyncPayload.WriteOffStock writeOff = requirePayload(event.writeOff(), "writeOff");
        Id inventoryItemId = idGenerator.convertIdFrom(event.inventoryItemId());
        Id createdBy = idGenerator.convertIdFrom(event.createdBy());
        InventoryItemResult result = commandBus.dispatch(new WriteOffStockCommand(
                inventoryItemId,
                writeOff.quantity(),
                writeOff.reason(),
                writeOff.notes(),
                createdBy,
                event.scopeKey(),
                event.scopeId()
        ));
        return maybeReorder(event, inventoryItemId, createdBy, result);
    }

    private InventoryItemResult reorderOnly(RequestSyncInventoryItemEvent event) {
        Id inventoryItemId = idGenerator.convertIdFrom(event.inventoryItemId());
        Id createdBy = idGenerator.convertIdFrom(event.createdBy());
        return dispatchReorder(event, inventoryItemId, createdBy);
    }

    private InventoryItemResult maybeReorder(
            RequestSyncInventoryItemEvent event,
            Id inventoryItemId,
            Id createdBy,
            InventoryItemResult result
    ) {
        if (event.reorder() == null) {
            return result;
        }
        return dispatchReorder(event, inventoryItemId, createdBy);
    }

    private InventoryItemResult dispatchReorder(
            RequestSyncInventoryItemEvent event,
            Id inventoryItemId,
            Id createdBy
    ) {
        InventorySyncPayload.Reorder reorder = requirePayload(event.reorder(), "reorder");
        return commandBus.dispatch(new UpdateReorderConfigCommand(
                inventoryItemId,
                defaultInt(reorder.safetyStock()),
                defaultInt(reorder.reorderPoint()),
                defaultInt(reorder.reorderQuantity()),
                reorder.maxStock(),
                createdBy,
                event.scopeKey(),
                event.scopeId()
        ));
    }

    private <T> T requirePayload(T payload, String field) {
        if (payload == null) {
            throw new IllegalArgumentException(field + " payload is required");
        }
        return payload;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
