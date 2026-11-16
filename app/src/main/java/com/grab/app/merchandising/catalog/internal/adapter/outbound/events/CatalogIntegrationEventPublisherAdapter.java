package com.grab.app.merchandising.catalog.internal.adapter.outbound.events;

import com.catalog.application.port.outbound.CatalogIntegrationEventPublisher;
import com.catalog.domain.event.CategoryChangedEvent;
import com.catalog.domain.event.ProductDeletedEvent;
import com.catalog.domain.event.ProductMediaChangedEvent;
import com.catalog.domain.event.ProductPublishedToChannelEvent;
import com.catalog.domain.event.ProductStatusChangedEvent;
import com.catalog.domain.event.ProductUnpublishedFromChannelEvent;
import com.catalog.domain.event.ProductUpdatedEvent;
import com.catalog.domain.event.ProductVariantAddedEvent;
import com.catalog.domain.event.ProductVariantChangeEvent;
import com.catalog.domain.event.ProductVariantDeletedEvent;
import com.catalog.domain.event.ProductVariantRestoredEvent;
import com.catalog.domain.event.VariantMediaChangedEvent;
import com.grab.app.shared.events.catalog.ProductDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductPresentationChangedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductPublishedToChannelIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductStatusChangedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductUnpublishedFromChannelIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantAddedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantRestoredIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantUpdatedIntegrationEvent;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class CatalogIntegrationEventPublisherAdapter implements CatalogIntegrationEventPublisher {
    private static final int EVENT_VERSION = 1;

    private final DomainEventProducer outbox;

    public CatalogIntegrationEventPublisherAdapter(
            @Qualifier("catalogDomainEventProducer") DomainEventProducer outbox
    ) {
        this.outbox = outbox;
    }

    @Override
    @EventListener
    public void publishProductVariantAdded(ProductVariantAddedEvent event) {
        var integrationEvent = new ProductVariantAddedIntegrationEvent(
                valueOf(event.productId()),
                valueOf(event.variantId()),
                event.sku(),
                event.productName(),
                event.manageInventory(),
                Instant.now(),
                EVENT_VERSION
        );
        outbox.produce("Product", valueOf(event.productId()), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishProductVariantChanged(ProductVariantChangeEvent event) {
        var integrationEvent = new ProductVariantUpdatedIntegrationEvent(
                valueOf(event.productId()),
                valueOf(event.variantId()),
                event.sku(),
                event.manageInventory(),
                Instant.now(),
                EVENT_VERSION
        );
        outbox.produce("Product", valueOf(event.productId()), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishProductVariantDeleted(ProductVariantDeletedEvent event) {
        var integrationEvent = new ProductVariantDeletedIntegrationEvent(
                valueOf(event.productId()),
                valueOf(event.variantId()),
                Instant.now(),
                EVENT_VERSION
        );
        outbox.produce("Product", valueOf(event.productId()), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishProductVariantRestored(ProductVariantRestoredEvent event) {
        var integrationEvent = new ProductVariantRestoredIntegrationEvent(
                valueOf(event.productId()),
                valueOf(event.variantId()),
                Instant.now(),
                EVENT_VERSION
        );
        outbox.produce("Product", valueOf(event.productId()), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishProductUpdated(ProductUpdatedEvent event) {
        var integrationEvent = new ProductPresentationChangedIntegrationEvent(
                valueOf(event.productId()),
                event.newName(),
                Instant.now(),
                EVENT_VERSION
        );
        outbox.produce("Product", valueOf(event.productId()), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishProductMediaChanged(ProductMediaChangedEvent event) {
        publishProductPresentationChanged(event.productId());
    }

    @Override
    @EventListener
    public void publishCategoryChanged(CategoryChangedEvent event) {
        publishProductPresentationChanged(event.productId());
    }

    @Override
    @EventListener
    public void publishVariantMediaChanged(VariantMediaChangedEvent event) {
        publishProductPresentationChanged(event.productId());
    }

    @Override
    @EventListener
    public void publishProductDeleted(ProductDeletedEvent event) {
        var integrationEvent = new ProductDeletedIntegrationEvent(
                valueOf(event.productId()),
                event.variantIds() == null
                        ? List.of()
                        : event.variantIds().stream().map(CatalogIntegrationEventPublisherAdapter::valueOf).toList(),
                Instant.now(),
                EVENT_VERSION
        );
        outbox.produce("Product", valueOf(event.productId()), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishProductPublishedToChannel(ProductPublishedToChannelEvent event) {
        var integrationEvent = new ProductPublishedToChannelIntegrationEvent(
                valueOf(event.variantId()),
                valueOf(event.salesChannelId()),
                Instant.now(),
                EVENT_VERSION
        );
        outbox.produce("ProductVariant", valueOf(event.variantId()), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishProductUnpublishedFromChannel(ProductUnpublishedFromChannelEvent event) {
        var integrationEvent = new ProductUnpublishedFromChannelIntegrationEvent(
                valueOf(event.variantId()),
                valueOf(event.salesChannelId()),
                Instant.now(),
                EVENT_VERSION
        );
        outbox.produce("ProductVariant", valueOf(event.variantId()), List.of(integrationEvent));
    }

    @Override
    @EventListener
    public void publishProductStatusChanged(ProductStatusChangedEvent event) {
        var integrationEvent = new ProductStatusChangedIntegrationEvent(
                valueOf(event.productId()),
                event.oldStatus(),
                event.newStatus(),
                Instant.now(),
                EVENT_VERSION
        );
        outbox.produce("Product", valueOf(event.productId()), List.of(integrationEvent));
    }

    private static String valueOf(Id id) {
        return id == null ? null : id.getValue();
    }

    private void publishProductPresentationChanged(Id productId) {
        var integrationEvent = new ProductPresentationChangedIntegrationEvent(
                valueOf(productId),
                null,
                Instant.now(),
                EVENT_VERSION
        );
        outbox.produce("Product", valueOf(productId), List.of(integrationEvent));
    }
}
