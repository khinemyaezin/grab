package com.grab.app.productmerchandising.inventory.internal.event;

import com.grab.framework.id.impl.CommonId;
import com.grab.app.shared.events.inventory.LocationLinkedToChannelIntegrationEvent;
import com.grab.app.shared.events.inventory.StockReceivedIntegrationEvent;
import com.grab.app.shared.events.inventory.StockReservedIntegrationEvent;
import com.inventory.domain.enums.AdjustmentReason;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.event.LocationLinkedToChannelEvent;
import com.inventory.domain.event.LocationDeactivatedEvent;
import com.inventory.domain.event.InventoryItemStatusChangedEvent;
import com.inventory.domain.event.StockAdjustedEvent;
import com.inventory.domain.event.StockReceivedEvent;
import com.inventory.domain.event.StockReservedEvent;
import com.grab.app.shared.events.inventory.StockAdjustedIntegrationEvent;
import com.grab.app.shared.events.inventory.InventoryStatusChangedIntegrationEvent;
import com.grab.app.shared.events.inventory.LocationStatusChangedIntegrationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryIntegrationEventPublisherTest {

    @Mock
    private ApplicationEventPublisher events;
    @Captor
    private ArgumentCaptor<Object> published;
    @InjectMocks
    private InventoryIntegrationEventPublisher publisher;

    @Test
    void handleStockReceived_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleStockReceived(new StockReceivedEvent(
                new CommonId("item-1"), "SKU-1", 4, new CommonId("loc-1"), LocalDateTime.now()));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(StockReceivedIntegrationEvent.class, event -> {
            assertThat(event.sku()).isEqualTo("SKU-1");
            assertThat(event.locationId()).isEqualTo("loc-1");
            assertThat(event.quantity()).isEqualTo(4);
        });
    }

    @Test
    void handleStockReserved_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleStockReserved(new StockReservedEvent(
                new CommonId("item-1"), "SKU-1", 1, "order-1", LocalDateTime.now()));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(StockReservedIntegrationEvent.class, event ->
                assertThat(event.sku()).isEqualTo("SKU-1"));
    }

    @Test
    void handleStockAdjusted_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleStockAdjusted(new StockAdjustedEvent(
                new CommonId("item-1"), "SKU-1", 5, 3, AdjustmentReason.DAMAGED, LocalDateTime.now()));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(StockAdjustedIntegrationEvent.class, event ->
                assertThat(event.reason()).isEqualTo("DAMAGED"));
    }

    @Test
    void handleLocationLinked_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleLocationLinked(new LocationLinkedToChannelEvent(
                new CommonId("loc-1"), new CommonId("channel-1"), LocalDateTime.now()));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(LocationLinkedToChannelIntegrationEvent.class, event -> {
            assertThat(event.locationId()).isEqualTo("loc-1");
            assertThat(event.salesChannelId()).isEqualTo("channel-1");
        });
    }

    @Test
    void handleInventoryStatusChanged_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleInventoryStatusChanged(new InventoryItemStatusChangedEvent(
                new CommonId("item-1"),
                "SKU-1",
                InventoryStatus.SUSPENDED,
                LocalDateTime.now()
        ));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(InventoryStatusChangedIntegrationEvent.class, event -> {
            assertThat(event.sku()).isEqualTo("SKU-1");
            assertThat(event.status()).isEqualTo("SUSPENDED");
        });
    }

    @Test
    void handleLocationDeactivated_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleLocationDeactivated(new LocationDeactivatedEvent(
                new CommonId("location-1"),
                "WAREHOUSE-1",
                LocalDateTime.now()
        ));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(LocationStatusChangedIntegrationEvent.class, event -> {
            assertThat(event.locationId()).isEqualTo("location-1");
            assertThat(event.active()).isFalse();
        });
    }
}
