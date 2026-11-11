package com.grab.app.merchandising.inventory.internal.adapter.outbound.events;

import com.grab.app.shared.events.inventory.InventoryStatusChangedIntegrationEvent;
import com.grab.app.shared.events.inventory.LocationLinkedToChannelIntegrationEvent;
import com.grab.app.shared.events.inventory.LocationStatusChangedIntegrationEvent;
import com.grab.app.shared.events.inventory.LocationUnlinkedFromChannelIntegrationEvent;
import com.grab.app.shared.events.inventory.StockAdjustedIntegrationEvent;
import com.grab.app.shared.events.inventory.StockReceivedIntegrationEvent;
import com.grab.app.shared.events.inventory.StockReservedIntegrationEvent;
import com.grab.app.shared.events.inventory.StockShippedIntegrationEvent;
import com.grab.framework.id.Id;
import com.inventory.application.port.outbound.InventoryIntegrationEventPublisher;
import com.inventory.domain.event.InventoryItemStatusChangedEvent;
import com.inventory.domain.event.LocationActivatedEvent;
import com.inventory.domain.event.LocationDeactivatedEvent;
import com.inventory.domain.event.LocationDeletedEvent;
import com.inventory.domain.event.LocationLinkedToChannelEvent;
import com.inventory.domain.event.LocationUnlinkedFromChannelEvent;
import com.inventory.domain.event.StockAdjustedEvent;
import com.inventory.domain.event.StockReceivedEvent;
import com.inventory.domain.event.StockReservedEvent;
import com.inventory.domain.event.StockShippedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class InventoryIntegrationEventPublisherAdapter implements InventoryIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    public InventoryIntegrationEventPublisherAdapter(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Override
    @EventListener
    public void publishStockReceived(StockReceivedEvent event) {
        events.publishEvent(new StockReceivedIntegrationEvent(
                valueOf(event.inventoryItemId()),
                event.sku(),
                event.quantity(),
                valueOf(event.locationId()),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        ));
    }

    @Override
    @EventListener
    public void publishStockReserved(StockReservedEvent event) {
        events.publishEvent(new StockReservedIntegrationEvent(
                valueOf(event.inventoryItemId()),
                event.sku(),
                event.quantity(),
                event.orderId(),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        ));
    }

    @Override
    @EventListener
    public void publishStockAdjusted(StockAdjustedEvent event) {
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

    @Override
    @EventListener
    public void publishStockShipped(StockShippedEvent event) {
        events.publishEvent(new StockShippedIntegrationEvent(
                valueOf(event.inventoryItemId()),
                event.sku(),
                event.quantity(),
                event.orderId(),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        ));
    }

    @Override
    @EventListener
    public void publishLocationLinked(LocationLinkedToChannelEvent event) {
        events.publishEvent(new LocationLinkedToChannelIntegrationEvent(
                valueOf(event.locationId()),
                valueOf(event.salesChannelId()),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        ));
    }

    @Override
    @EventListener
    public void publishLocationUnlinked(LocationUnlinkedFromChannelEvent event) {
        events.publishEvent(new LocationUnlinkedFromChannelIntegrationEvent(
                valueOf(event.locationId()),
                valueOf(event.salesChannelId()),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        ));
    }

    @Override
    @EventListener
    public void publishInventoryStatusChanged(InventoryItemStatusChangedEvent event) {
        events.publishEvent(new InventoryStatusChangedIntegrationEvent(
                event.sku(),
                event.status() == null ? null : event.status().name(),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        ));
    }

    @Override
    @EventListener
    public void publishLocationActivated(LocationActivatedEvent event) {
        publishLocationStatusChanged(event.locationId(), true, event.occurredAt());
    }

    @Override
    @EventListener
    public void publishLocationDeactivated(LocationDeactivatedEvent event) {
        publishLocationStatusChanged(event.locationId(), false, event.occurredAt());
    }

    @Override
    @EventListener
    public void publishLocationDeleted(LocationDeletedEvent event) {
        publishLocationStatusChanged(event.locationId(), false, event.occurredAt());
    }

    private void publishLocationStatusChanged(Id locationId, boolean active, LocalDateTime occurredAt) {
        LocationStatusChangedIntegrationEvent event = new LocationStatusChangedIntegrationEvent(
                valueOf(locationId),
                active,
                toInstant(occurredAt),
                EVENT_VERSION
        );
        events.publishEvent(event);
    }

    private static String valueOf(Id id) {
        return id == null ? null : id.getValue();
    }

    private static Instant toInstant(LocalDateTime occurredAt) {
        return occurredAt == null ? Instant.now() : occurredAt.toInstant(ZoneOffset.UTC);
    }
}
