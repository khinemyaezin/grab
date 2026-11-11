package com.grab.app.merchandising.inventory.internal.adapter.outbound.events;

import com.grab.app.shared.events.inventory.InventoryStatusChangedIntegrationEvent;
import com.grab.app.shared.events.inventory.LocationLinkedToChannelIntegrationEvent;
import com.grab.app.shared.events.inventory.LocationStatusChangedIntegrationEvent;
import com.grab.app.shared.events.inventory.LocationUnlinkedFromChannelIntegrationEvent;
import com.grab.app.shared.events.inventory.StockAdjustedIntegrationEvent;
import com.grab.app.shared.events.inventory.StockReceivedIntegrationEvent;
import com.grab.app.shared.events.inventory.StockReservedIntegrationEvent;
import com.grab.app.shared.events.inventory.StockShippedIntegrationEvent;
import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.enums.AdjustmentReason;
import com.inventory.domain.enums.InventoryStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryIntegrationEventPublisherAdapterTest {

    private static final LocalDateTime NOW = LocalDateTime.parse("2026-10-01T12:00:00");

    @Mock
    private ApplicationEventPublisher events;

    @Captor
    private ArgumentCaptor<Object> eventCaptor;

    private InventoryIntegrationEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InventoryIntegrationEventPublisherAdapter(events);
    }

    @Test
    void publishStockReceived_publishesIntegrationEvent() {
        adapter.publishStockReceived(new StockReceivedEvent(
                new CommonId("item-1"), "SKU-1", 4, new CommonId("loc-1"), NOW
        ));

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(StockReceivedIntegrationEvent.class, event -> {
            assertThat(event.inventoryItemId()).isEqualTo("item-1");
            assertThat(event.sku()).isEqualTo("SKU-1");
            assertThat(event.quantity()).isEqualTo(4);
            assertThat(event.locationId()).isEqualTo("loc-1");
            assertThat(event.version()).isEqualTo(1);
        });
    }

    @Test
    void publishStockReserved_publishesIntegrationEvent() {
        adapter.publishStockReserved(new StockReservedEvent(
                new CommonId("item-1"), "SKU-1", 2, "order-1", NOW
        ));

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(StockReservedIntegrationEvent.class, event -> {
            assertThat(event.inventoryItemId()).isEqualTo("item-1");
            assertThat(event.sku()).isEqualTo("SKU-1");
            assertThat(event.quantity()).isEqualTo(2);
            assertThat(event.orderId()).isEqualTo("order-1");
        });
    }

    @Test
    void publishStockAdjusted_publishesIntegrationEvent() {
        adapter.publishStockAdjusted(new StockAdjustedEvent(
                new CommonId("item-1"), "SKU-1", 5, 3, AdjustmentReason.DAMAGED, NOW
        ));

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(StockAdjustedIntegrationEvent.class, event -> {
            assertThat(event.inventoryItemId()).isEqualTo("item-1");
            assertThat(event.sku()).isEqualTo("SKU-1");
            assertThat(event.previousQuantity()).isEqualTo(5);
            assertThat(event.newQuantity()).isEqualTo(3);
            assertThat(event.reason()).isEqualTo("DAMAGED");
        });
    }

    @Test
    void publishStockShipped_publishesIntegrationEvent() {
        adapter.publishStockShipped(new StockShippedEvent(
                new CommonId("item-1"), "SKU-1", 1, "order-1", NOW
        ));

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(StockShippedIntegrationEvent.class, event -> {
            assertThat(event.inventoryItemId()).isEqualTo("item-1");
            assertThat(event.sku()).isEqualTo("SKU-1");
            assertThat(event.quantity()).isEqualTo(1);
            assertThat(event.orderId()).isEqualTo("order-1");
        });
    }

    @Test
    void publishLocationLinked_publishesIntegrationEvent() {
        adapter.publishLocationLinked(new LocationLinkedToChannelEvent(
                new CommonId("loc-1"), new CommonId("channel-1"), NOW
        ));

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(LocationLinkedToChannelIntegrationEvent.class, event -> {
            assertThat(event.locationId()).isEqualTo("loc-1");
            assertThat(event.salesChannelId()).isEqualTo("channel-1");
        });
    }

    @Test
    void publishLocationUnlinked_publishesIntegrationEvent() {
        adapter.publishLocationUnlinked(new LocationUnlinkedFromChannelEvent(
                new CommonId("loc-1"), new CommonId("channel-1"), NOW
        ));

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(LocationUnlinkedFromChannelIntegrationEvent.class, event -> {
            assertThat(event.locationId()).isEqualTo("loc-1");
            assertThat(event.salesChannelId()).isEqualTo("channel-1");
        });
    }

    @Test
    void publishInventoryStatusChanged_publishesIntegrationEvent() {
        adapter.publishInventoryStatusChanged(new InventoryItemStatusChangedEvent(
                new CommonId("item-1"), "SKU-1", InventoryStatus.SUSPENDED, NOW
        ));

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(InventoryStatusChangedIntegrationEvent.class, event -> {
            assertThat(event.sku()).isEqualTo("SKU-1");
            assertThat(event.status()).isEqualTo("SUSPENDED");
        });
    }

    @Test
    void publishLocationActivated_publishesLocationStatusChanged() {
        adapter.publishLocationActivated(new LocationActivatedEvent(
                new CommonId("loc-1"), "Warehouse A", NOW
        ));

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(LocationStatusChangedIntegrationEvent.class, event -> {
            assertThat(event.locationId()).isEqualTo("loc-1");
            assertThat(event.active()).isTrue();
        });
    }

    @Test
    void publishLocationDeactivated_publishesLocationStatusChanged() {
        adapter.publishLocationDeactivated(new LocationDeactivatedEvent(
                new CommonId("loc-1"), "Warehouse A", NOW
        ));

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(LocationStatusChangedIntegrationEvent.class, event -> {
            assertThat(event.locationId()).isEqualTo("loc-1");
            assertThat(event.active()).isFalse();
        });
    }

    @Test
    void publishLocationDeleted_publishesLocationStatusChanged() {
        adapter.publishLocationDeleted(new LocationDeletedEvent(
                new CommonId("loc-1"), "Warehouse A", NOW
        ));

        verify(events).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(LocationStatusChangedIntegrationEvent.class, event -> {
            assertThat(event.locationId()).isEqualTo("loc-1");
            assertThat(event.active()).isFalse();
        });
    }
}
