package com.grab.store.catalog.internal.event;

import com.catalog.domain.event.ProductDeletedEvent;
import com.catalog.domain.event.ProductUpdatedEvent;
import com.catalog.domain.event.ProductVariantAddedEvent;
import com.catalog.domain.event.ProductVariantChangeEvent;
import com.catalog.domain.event.ProductVariantDeletedEvent;
import com.catalog.domain.event.ProductVariantRestoredEvent;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.events.ProductDeletedIntegrationEvent;
import com.grab.store.catalog.events.ProductNameChangedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantAddedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantDeletedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantRestoredIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantUpdatedIntegrationEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogIntegrationEventPublisherTest {

    private final AtomicReference<Object> published = new AtomicReference<>();
    private final ApplicationEventPublisher events = published::set;
    private final CatalogIntegrationEventPublisher publisher = new CatalogIntegrationEventPublisher(events);

    @Test
    void handleProductVariantAdded_shouldPublishIntegrationEvent() {
        publisher.handleProductVariantAdded(new ProductVariantAddedEvent(
                new CommonId("product-1"), new CommonId("variant-1"), "SKU001", "T-Shirt"));

        assertThat(published.get()).isInstanceOfSatisfying(ProductVariantAddedIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.variantId()).isEqualTo("variant-1");
            assertThat(event.sku()).isEqualTo("SKU001");
            assertThat(event.productName()).isEqualTo("T-Shirt");
            assertThat(event.occurredAt()).isNotNull();
            assertThat(event.version()).isEqualTo(1);
        });
    }

    @Test
    void handleProductVariantChanged_shouldPublishIntegrationEvent() {
        publisher.handleProductVariantChanged(new ProductVariantChangeEvent(
                new CommonId("product-1"), new CommonId("variant-1"), "SKU002"));

        assertThat(published.get()).isInstanceOfSatisfying(ProductVariantUpdatedIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.variantId()).isEqualTo("variant-1");
            assertThat(event.sku()).isEqualTo("SKU002");
        });
    }

    @Test
    void handleProductVariantDeleted_shouldPublishIntegrationEvent() {
        publisher.handleProductVariantDeleted(new ProductVariantDeletedEvent(
                new CommonId("product-1"), new CommonId("category-1"), new CommonId("variant-1")));

        assertThat(published.get()).isInstanceOfSatisfying(ProductVariantDeletedIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.variantId()).isEqualTo("variant-1");
        });
    }

    @Test
    void handleProductVariantRestored_shouldPublishIntegrationEvent() {
        publisher.handleProductVariantRestored(new ProductVariantRestoredEvent(
                new CommonId("product-1"), new CommonId("variant-1")));

        assertThat(published.get()).isInstanceOfSatisfying(ProductVariantRestoredIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.variantId()).isEqualTo("variant-1");
        });
    }

    @Test
    void handleProductUpdated_shouldPublishNameChangeIntegrationEvent() {
        publisher.handleProductUpdated(new ProductUpdatedEvent(
                new CommonId("product-1"), "New Name", new CommonId("category-1")));

        assertThat(published.get()).isInstanceOfSatisfying(ProductNameChangedIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.newName()).isEqualTo("New Name");
        });
    }

    @Test
    void handleProductDeleted_shouldPublishIntegrationEventWithVariantIds() {
        publisher.handleProductDeleted(new ProductDeletedEvent(
                new CommonId("product-1"),
                new CommonId("category-1"),
                List.of(new CommonId("variant-1"), new CommonId("variant-2"))));

        assertThat(published.get()).isInstanceOfSatisfying(ProductDeletedIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.variantIds()).containsExactly("variant-1", "variant-2");
        });
    }
}
