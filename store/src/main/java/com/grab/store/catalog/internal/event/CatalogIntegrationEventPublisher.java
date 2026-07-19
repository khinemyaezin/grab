package com.grab.store.catalog.internal.event;

import com.catalog.domain.event.ProductDeletedEvent;
import com.catalog.domain.event.ProductUpdatedEvent;
import com.catalog.domain.event.ProductVariantAddedEvent;
import com.catalog.domain.event.ProductVariantChangeEvent;
import com.catalog.domain.event.ProductVariantDeletedEvent;
import com.catalog.domain.event.ProductVariantRestoredEvent;
import com.grab.framework.id.Id;
import com.grab.store.catalog.events.ProductDeletedIntegrationEvent;
import com.grab.store.catalog.events.ProductNameChangedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantAddedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantDeletedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantRestoredIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantUpdatedIntegrationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final ApplicationEventPublisher events;

    @EventListener
    public void handleProductVariantAdded(ProductVariantAddedEvent event) {
        events.publishEvent(new ProductVariantAddedIntegrationEvent(
                valueOf(event.productId()),
                valueOf(event.variantId()),
                event.sku(),
                event.productName(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleProductVariantChanged(ProductVariantChangeEvent event) {
        events.publishEvent(new ProductVariantUpdatedIntegrationEvent(
                valueOf(event.productId()),
                valueOf(event.variantId()),
                event.sku(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleProductVariantDeleted(ProductVariantDeletedEvent event) {
        events.publishEvent(new ProductVariantDeletedIntegrationEvent(
                valueOf(event.productId()),
                valueOf(event.variantId()),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleProductVariantRestored(ProductVariantRestoredEvent event) {
        events.publishEvent(new ProductVariantRestoredIntegrationEvent(
                valueOf(event.productId()),
                valueOf(event.variantId()),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleProductUpdated(ProductUpdatedEvent event) {
        events.publishEvent(new ProductNameChangedIntegrationEvent(
                valueOf(event.productId()),
                event.newName(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleProductDeleted(ProductDeletedEvent event) {
        events.publishEvent(new ProductDeletedIntegrationEvent(
                valueOf(event.productId()),
                event.variantIds() == null
                        ? List.of()
                        : event.variantIds().stream().map(CatalogIntegrationEventPublisher::valueOf).toList(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private static String valueOf(Id id) {
        return id == null ? null : id.getValue();
    }
}
