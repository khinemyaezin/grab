package com.grab.store.inventory.internal.event;

import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.events.LocationLinkedToChannelIntegrationEvent;
import com.grab.store.inventory.events.StockReceivedIntegrationEvent;
import com.grab.store.inventory.events.StockReservedIntegrationEvent;
import com.inventory.domain.enums.AdjustmentReason;
import com.inventory.domain.event.LocationLinkedToChannelEvent;
import com.inventory.domain.event.StockAdjustedEvent;
import com.inventory.domain.event.StockReceivedEvent;
import com.inventory.domain.event.StockReservedEvent;
import com.grab.store.inventory.events.StockAdjustedIntegrationEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryIntegrationEventPublisherTest {

    private final AtomicReference<Object> published = new AtomicReference<>();
    private final ApplicationEventPublisher events = published::set;
    private final InventoryIntegrationEventPublisher publisher = new InventoryIntegrationEventPublisher(events);

    @Test
    void handleStockReceived_shouldPublishIntegrationEvent() {
        publisher.handleStockReceived(new StockReceivedEvent(
                new CommonId("item-1"), "SKU-1", 4, new CommonId("loc-1"), LocalDateTime.now()));

        assertThat(published.get()).isInstanceOfSatisfying(StockReceivedIntegrationEvent.class, event -> {
            assertThat(event.sku()).isEqualTo("SKU-1");
            assertThat(event.locationId()).isEqualTo("loc-1");
            assertThat(event.quantity()).isEqualTo(4);
        });
    }

    @Test
    void handleStockReserved_shouldPublishIntegrationEvent() {
        publisher.handleStockReserved(new StockReservedEvent(
                new CommonId("item-1"), "SKU-1", 1, "order-1", LocalDateTime.now()));

        assertThat(published.get()).isInstanceOfSatisfying(StockReservedIntegrationEvent.class, event ->
                assertThat(event.sku()).isEqualTo("SKU-1"));
    }

    @Test
    void handleStockAdjusted_shouldPublishIntegrationEvent() {
        publisher.handleStockAdjusted(new StockAdjustedEvent(
                new CommonId("item-1"), "SKU-1", 5, 3, AdjustmentReason.DAMAGED, LocalDateTime.now()));

        assertThat(published.get()).isInstanceOfSatisfying(StockAdjustedIntegrationEvent.class, event ->
                assertThat(event.reason()).isEqualTo("DAMAGED"));
    }

    @Test
    void handleLocationLinked_shouldPublishIntegrationEvent() {
        publisher.handleLocationLinked(new LocationLinkedToChannelEvent(
                new CommonId("loc-1"), new CommonId("channel-1"), LocalDateTime.now()));

        assertThat(published.get()).isInstanceOfSatisfying(LocationLinkedToChannelIntegrationEvent.class, event -> {
            assertThat(event.locationId()).isEqualTo("loc-1");
            assertThat(event.salesChannelId()).isEqualTo("channel-1");
        });
    }
}
