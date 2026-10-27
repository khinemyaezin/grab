package com.grab.app.catalog.catalog.internal.adapter.outbound.event;

import com.catalog.domain.event.ProductDeletedEvent;
import com.catalog.domain.event.ProductMediaChangedEvent;
import com.catalog.domain.event.ProductPublishedToChannelEvent;
import com.catalog.domain.event.CategoryChangedEvent;
import com.catalog.domain.event.ProductStatusChangedEvent;
import com.catalog.domain.event.ProductUnpublishedFromChannelEvent;
import com.catalog.domain.event.ProductUpdatedEvent;
import com.catalog.domain.event.ProductVariantAddedEvent;
import com.catalog.domain.event.ProductVariantChangeEvent;
import com.catalog.domain.event.ProductVariantDeletedEvent;
import com.catalog.domain.event.ProductVariantRestoredEvent;
import com.catalog.domain.event.VariantMediaChangedEvent;
import com.grab.framework.id.Id;
import com.grab.app.shared.events.catalog.ProductDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductPresentationChangedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductPublishedToChannelIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductStatusChangedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductUnpublishedFromChannelIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantAddedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantRestoredIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantUpdatedIntegrationEvent;
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
                event.manageInventory(),
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
                event.manageInventory(),
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
        events.publishEvent(new ProductPresentationChangedIntegrationEvent(
                valueOf(event.productId()),
                event.newName(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleProductMediaChanged(ProductMediaChangedEvent event) {
        publishProductPresentationChanged(event.productId());
    }

    @EventListener
    public void handleCategoryChanged(CategoryChangedEvent event) {
        publishProductPresentationChanged(event.productId());
    }

    @EventListener
    public void handleVariantMediaChanged(VariantMediaChangedEvent event) {
        publishProductPresentationChanged(event.productId());
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

    @EventListener
    public void handleProductPublishedToChannel(ProductPublishedToChannelEvent event) {
        events.publishEvent(new ProductPublishedToChannelIntegrationEvent(
                valueOf(event.variantId()),
                valueOf(event.salesChannelId()),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleProductUnpublishedFromChannel(ProductUnpublishedFromChannelEvent event) {
        events.publishEvent(new ProductUnpublishedFromChannelIntegrationEvent(
                valueOf(event.variantId()),
                valueOf(event.salesChannelId()),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    @EventListener
    public void handleProductStatusChanged(ProductStatusChangedEvent event) {
        events.publishEvent(new ProductStatusChangedIntegrationEvent(
                valueOf(event.productId()),
                event.oldStatus(),
                event.newStatus(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private static String valueOf(Id id) {
        return id == null ? null : id.getValue();
    }

    private void publishProductPresentationChanged(Id productId) {
        events.publishEvent(new ProductPresentationChangedIntegrationEvent(
                valueOf(productId),
                null,
                Instant.now(),
                EVENT_VERSION
        ));
    }
}
