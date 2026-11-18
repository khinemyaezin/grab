package com.grab.service.merchandising.inventory.internal.adapter.outbound.events;

import com.grab.service.contract.event.merchandising.inventory.InventoryStatusChangedIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.LocationLinkedToChannelIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.LocationStatusChangedIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.LocationUnlinkedFromChannelIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.StockAdjustedIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.StockReceivedIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.StockReservedIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.StockShippedIntegrationEvent;
import com.grab.framework.event.DomainEventProducer;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class InventoryIntegrationEventPublisherAdapter implements InventoryIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final DomainEventProducer outbox;

    public InventoryIntegrationEventPublisherAdapter(
            @Qualifier("inventoryDomainEventProducer") DomainEventProducer outbox
    ) {
        this.outbox = outbox;
    }

    @Override
    @EventListener
    public void publishStockReceived(StockReceivedEvent event) {
        var integrationEvent = new StockReceivedIntegrationEvent(
                valueOf(event.inventoryItemId()),
                event.sku(),
                event.quantity(),
                valueOf(event.locationId()),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        );
        outbox.produce("InventoryItem", event.sku(), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishStockReserved(StockReservedEvent event) {
        var integrationEvent = new StockReservedIntegrationEvent(
                valueOf(event.inventoryItemId()),
                event.sku(),
                event.quantity(),
                event.orderId(),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        );
        outbox.produce("InventoryItem", event.sku(), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishStockAdjusted(StockAdjustedEvent event) {
        var integrationEvent = new StockAdjustedIntegrationEvent(
                valueOf(event.inventoryItemId()),
                event.sku(),
                event.previousQuantity(),
                event.newQuantity(),
                event.reason() == null ? null : event.reason().name(),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        );
        outbox.produce("InventoryItem", event.sku(), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishStockShipped(StockShippedEvent event) {
        var integrationEvent = new StockShippedIntegrationEvent(
                valueOf(event.inventoryItemId()),
                event.sku(),
                event.quantity(),
                event.orderId(),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        );
        outbox.produce("InventoryItem", event.sku(), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishLocationLinked(LocationLinkedToChannelEvent event) {
        var integrationEvent = new LocationLinkedToChannelIntegrationEvent(
                valueOf(event.locationId()),
                valueOf(event.salesChannelId()),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        );
        outbox.produce("InventoryLocation", valueOf(event.locationId()), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishLocationUnlinked(LocationUnlinkedFromChannelEvent event) {
        var integrationEvent = new LocationUnlinkedFromChannelIntegrationEvent(
                valueOf(event.locationId()),
                valueOf(event.salesChannelId()),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        );
        outbox.produce("InventoryLocation", valueOf(event.locationId()), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishInventoryStatusChanged(InventoryItemStatusChangedEvent event) {
        var integrationEvent = new InventoryStatusChangedIntegrationEvent(
                event.sku(),
                event.status() == null ? null : event.status().name(),
                toInstant(event.occurredAt()),
                EVENT_VERSION
        );
        outbox.produce("InventoryItem", event.sku(), List.of(integrationEvent));
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
        outbox.produce("InventoryLocation", valueOf(locationId), List.of(event));
    }

    private static String valueOf(Id id) {
        return id == null ? null : id.getValue();
    }

    private static Instant toInstant(LocalDateTime occurredAt) {
        return occurredAt == null ? Instant.now() : occurredAt.toInstant(ZoneOffset.UTC);
    }
}
