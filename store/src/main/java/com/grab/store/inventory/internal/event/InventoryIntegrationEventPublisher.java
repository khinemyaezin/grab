package com.grab.store.inventory.internal.event;

import com.grab.framework.id.Id;
import com.grab.store.inventory.events.LocationLinkedToChannelIntegrationEvent;
import com.grab.store.inventory.events.LocationUnlinkedFromChannelIntegrationEvent;
import com.grab.store.inventory.events.StockAdjustedIntegrationEvent;
import com.grab.store.inventory.events.StockReceivedIntegrationEvent;
import com.grab.store.inventory.events.StockReservedIntegrationEvent;
import com.grab.store.inventory.events.StockShippedIntegrationEvent;
import com.inventory.domain.event.LocationLinkedToChannelEvent;
import com.inventory.domain.event.LocationUnlinkedFromChannelEvent;
import com.inventory.domain.event.StockAdjustedEvent;
import com.inventory.domain.event.StockReceivedEvent;
import com.inventory.domain.event.StockReservedEvent;
import com.inventory.domain.event.StockShippedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class InventoryIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    @EventListener
    public void handleStockReceived(StockReceivedEvent event) {
        events.publishEvent(new StockReceivedIntegrationEvent(
                valueOf(event.inventoryItemId()),
                event.sku(),
                event.quantity(),
                valueOf(event.locationId()),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleStockReserved(StockReservedEvent event) {
        events.publishEvent(new StockReservedIntegrationEvent(
                valueOf(event.inventoryItemId()),
                event.sku(),
                event.quantity(),
                event.orderId(),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleStockAdjusted(StockAdjustedEvent event) {
        events.publishEvent(new StockAdjustedIntegrationEvent(
                valueOf(event.inventoryItemId()),
                event.sku(),
                event.previousQuantity(),
                event.newQuantity(),
                event.reason() == null ? null : event.reason().name(),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleStockShipped(StockShippedEvent event) {
        events.publishEvent(new StockShippedIntegrationEvent(
                valueOf(event.inventoryItemId()),
                event.sku(),
                event.quantity(),
                event.orderId(),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleLocationLinked(LocationLinkedToChannelEvent event) {
        events.publishEvent(new LocationLinkedToChannelIntegrationEvent(
                valueOf(event.locationId()),
                valueOf(event.salesChannelId()),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleLocationUnlinked(LocationUnlinkedFromChannelEvent event) {
        events.publishEvent(new LocationUnlinkedFromChannelIntegrationEvent(
                valueOf(event.locationId()),
                valueOf(event.salesChannelId()),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        ));
    }

    private static String valueOf(Id id) {
        return id == null ? null : id.getValue();
    }

    private static Instant toInstant(LocalDateTime occurredAt) {
        return occurredAt == null ? Instant.now() : occurredAt.toInstant(ZoneOffset.UTC);
    }
}
