package com.grab.app.productmerchandising.catalog.internal.event;

import com.catalog.domain.event.ProductDeletedEvent;
import com.catalog.domain.event.ProductPublishedToChannelEvent;
import com.catalog.domain.event.ProductStatusChangedEvent;
import com.catalog.domain.event.ProductUnpublishedFromChannelEvent;
import com.catalog.domain.event.ProductUpdatedEvent;
import com.catalog.domain.event.ProductVariantAddedEvent;
import com.catalog.domain.event.ProductVariantChangeEvent;
import com.catalog.domain.event.ProductVariantDeletedEvent;
import com.catalog.domain.event.ProductVariantRestoredEvent;
import com.grab.framework.id.impl.CommonId;
import com.grab.app.shared.events.catalog.ProductDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductPresentationChangedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductPublishedToChannelIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductStatusChangedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductUnpublishedFromChannelIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantAddedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantRestoredIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantUpdatedIntegrationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CatalogIntegrationEventPublisherTest {

    @Mock
    private ApplicationEventPublisher events;
    @Captor
    private ArgumentCaptor<Object> published;
    @InjectMocks
    private CatalogIntegrationEventPublisher publisher;

    @Test
    void handleProductVariantAdded_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleProductVariantAdded(new ProductVariantAddedEvent(
                new CommonId("product-1"), new CommonId("variant-1"), "SKU001", "T-Shirt", true));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(ProductVariantAddedIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.variantId()).isEqualTo("variant-1");
            assertThat(event.sku()).isEqualTo("SKU001");
            assertThat(event.productName()).isEqualTo("T-Shirt");
            assertThat(event.manageInventory()).isTrue();
            assertThat(event.occurredAt()).isNotNull();
            assertThat(event.version()).isEqualTo(1);
        });
    }

    @Test
    void handleProductVariantChanged_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleProductVariantChanged(new ProductVariantChangeEvent(
                new CommonId("product-1"), new CommonId("variant-1"), "SKU002", false));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(ProductVariantUpdatedIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.variantId()).isEqualTo("variant-1");
            assertThat(event.sku()).isEqualTo("SKU002");
            assertThat(event.manageInventory()).isFalse();
        });
    }

    @Test
    void handleProductVariantDeleted_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleProductVariantDeleted(new ProductVariantDeletedEvent(
                new CommonId("product-1"), new CommonId("category-1"), new CommonId("variant-1")));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(ProductVariantDeletedIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.variantId()).isEqualTo("variant-1");
        });
    }

    @Test
    void handleProductVariantRestored_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleProductVariantRestored(new ProductVariantRestoredEvent(
                new CommonId("product-1"), new CommonId("variant-1")));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(ProductVariantRestoredIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.variantId()).isEqualTo("variant-1");
        });
    }

    @Test
    void handleProductUpdated_domainEvent_shouldPublishProductPresentationChangeIntegrationEvent() {
        publisher.handleProductUpdated(new ProductUpdatedEvent(
                new CommonId("product-1"), "New Name", new CommonId("category-1")));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(ProductPresentationChangedIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.productName()).isEqualTo("New Name");
        });
    }

    @Test
    void handleProductDeleted_domainEvent_shouldPublishIntegrationEventWithVariantIds() {
        publisher.handleProductDeleted(new ProductDeletedEvent(
                new CommonId("product-1"),
                new CommonId("category-1"),
                List.of(new CommonId("variant-1"), new CommonId("variant-2"))));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(ProductDeletedIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.variantIds()).containsExactly("variant-1", "variant-2");
        });
    }

    @Test
    void handleProductPublishedToChannel_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleProductPublishedToChannel(new ProductPublishedToChannelEvent(
                new CommonId("variant-1"), new CommonId("channel-1")));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(ProductPublishedToChannelIntegrationEvent.class, event -> {
            assertThat(event.variantId()).isEqualTo("variant-1");
            assertThat(event.salesChannelId()).isEqualTo("channel-1");
        });
    }

    @Test
    void handleProductUnpublishedFromChannel_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleProductUnpublishedFromChannel(new ProductUnpublishedFromChannelEvent(
                new CommonId("variant-1"), new CommonId("channel-1")));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(ProductUnpublishedFromChannelIntegrationEvent.class, event -> {
            assertThat(event.variantId()).isEqualTo("variant-1");
            assertThat(event.salesChannelId()).isEqualTo("channel-1");
        });
    }

    @Test
    void handleProductStatusChanged_domainEvent_shouldPublishIntegrationEvent() {
        publisher.handleProductStatusChanged(new ProductStatusChangedEvent(
                new CommonId("product-1"), "DRAFT", "ACTIVE"));

        verify(events).publishEvent(published.capture());
        assertThat(published.getValue()).isInstanceOfSatisfying(ProductStatusChangedIntegrationEvent.class, event -> {
            assertThat(event.productId()).isEqualTo("product-1");
            assertThat(event.oldStatus()).isEqualTo("DRAFT");
            assertThat(event.newStatus()).isEqualTo("ACTIVE");
        });
    }
}
