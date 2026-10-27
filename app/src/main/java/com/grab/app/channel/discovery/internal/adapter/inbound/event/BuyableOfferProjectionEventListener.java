package com.grab.app.channel.discovery.internal.adapter.inbound.event;

import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshChannel;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshLocation;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshProduct;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshSku;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshVariant;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.app.shared.events.catalog.ProductDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductPresentationChangedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductPublishedToChannelIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductStatusChangedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductUnpublishedFromChannelIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantAddedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantRestoredIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantUpdatedIntegrationEvent;
import com.grab.app.shared.events.inventory.LocationLinkedToChannelIntegrationEvent;
import com.grab.app.shared.events.inventory.LocationUnlinkedFromChannelIntegrationEvent;
import com.grab.app.shared.events.inventory.InventoryStatusChangedIntegrationEvent;
import com.grab.app.shared.events.inventory.LocationStatusChangedIntegrationEvent;
import com.grab.app.shared.events.inventory.StockAdjustedIntegrationEvent;
import com.grab.app.shared.events.inventory.StockReceivedIntegrationEvent;
import com.grab.app.shared.events.inventory.StockReservedIntegrationEvent;
import com.grab.app.shared.events.inventory.StockShippedIntegrationEvent;
import com.grab.app.shared.events.saleschannel.SalesChannelDisabledIntegrationEvent;
import com.grab.app.shared.events.saleschannel.SalesChannelEnabledIntegrationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BuyableOfferProjectionEventListener {
    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    @EventListener
    public void onPublished(ProductPublishedToChannelIntegrationEvent event) {
        Id variantId = idGenerator.convertIdFrom(event.variantId());
        Id channelId = idGenerator.convertIdFrom(event.salesChannelId());
        dispatch(new RefreshVariant(variantId, channelId, event.occurredAt()));
    }

    @EventListener
    public void onUnpublished(ProductUnpublishedFromChannelIntegrationEvent event) {
        Id variantId = idGenerator.convertIdFrom(event.variantId());
        Id channelId = idGenerator.convertIdFrom(event.salesChannelId());
        dispatch(new RefreshVariant(variantId, channelId, event.occurredAt()));
    }

    @EventListener
    public void onVariantAdded(ProductVariantAddedIntegrationEvent event) {
        Id productId = idGenerator.convertIdFrom(event.productId());
        dispatch(new RefreshProduct(productId, event.occurredAt()));
    }

    @EventListener
    public void onVariantUpdated(ProductVariantUpdatedIntegrationEvent event) {
        Id productId = idGenerator.convertIdFrom(event.productId());
        dispatch(new RefreshProduct(productId, event.occurredAt()));
    }

    @EventListener
    public void onVariantDeleted(ProductVariantDeletedIntegrationEvent event) {
        Id productId = idGenerator.convertIdFrom(event.productId());
        dispatch(new RefreshProduct(productId, event.occurredAt()));
    }

    @EventListener
    public void onVariantRestored(ProductVariantRestoredIntegrationEvent event) {
        Id productId = idGenerator.convertIdFrom(event.productId());
        dispatch(new RefreshProduct(productId, event.occurredAt()));
    }

    @EventListener
    public void onProductDeleted(ProductDeletedIntegrationEvent event) {
        Id productId = idGenerator.convertIdFrom(event.productId());
        dispatch(new RefreshProduct(productId, event.occurredAt()));
    }

    @EventListener
    public void onProductPresentationChanged(ProductPresentationChangedIntegrationEvent event) {
        Id productId = idGenerator.convertIdFrom(event.productId());
        dispatch(new RefreshProduct(productId, event.occurredAt()));
    }

    @EventListener
    public void onProductStatusChanged(ProductStatusChangedIntegrationEvent event) {
        Id productId = idGenerator.convertIdFrom(event.productId());
        dispatch(new RefreshProduct(productId, event.occurredAt()));
    }

    @EventListener
    public void onStockReceived(StockReceivedIntegrationEvent event) {
        dispatch(new RefreshSku(event.sku(), event.occurredAt()));
    }

    @EventListener
    public void onStockReserved(StockReservedIntegrationEvent event) {
        dispatch(new RefreshSku(event.sku(), event.occurredAt()));
    }

    @EventListener
    public void onStockAdjusted(StockAdjustedIntegrationEvent event) {
        dispatch(new RefreshSku(event.sku(), event.occurredAt()));
    }

    @EventListener
    public void onStockShipped(StockShippedIntegrationEvent event) {
        dispatch(new RefreshSku(event.sku(), event.occurredAt()));
    }

    @EventListener
    public void onInventoryStatusChanged(InventoryStatusChangedIntegrationEvent event) {
        dispatch(new RefreshSku(event.sku(), event.occurredAt()));
    }

    @EventListener
    public void onLocationStatusChanged(LocationStatusChangedIntegrationEvent event) {
        Id locationId = idGenerator.convertIdFrom(event.locationId());
        dispatch(new RefreshLocation(locationId, null, event.occurredAt()));
    }

    @EventListener
    public void onLocationLinked(LocationLinkedToChannelIntegrationEvent event) {
        Id locationId = idGenerator.convertIdFrom(event.locationId());
        Id channelId = idGenerator.convertIdFrom(event.salesChannelId());
        dispatch(new RefreshLocation(locationId, channelId, event.occurredAt()));
    }

    @EventListener
    public void onLocationUnlinked(LocationUnlinkedFromChannelIntegrationEvent event) {
        Id locationId = idGenerator.convertIdFrom(event.locationId());
        Id channelId = idGenerator.convertIdFrom(event.salesChannelId());
        dispatch(new RefreshLocation(locationId, channelId, event.occurredAt()));
    }

    @EventListener
    public void onChannelEnabled(SalesChannelEnabledIntegrationEvent event) {
        Id channelId = idGenerator.convertIdFrom(event.salesChannelId());
        dispatch(new RefreshChannel(channelId, event.occurredAt()));
    }

    @EventListener
    public void onChannelDisabled(SalesChannelDisabledIntegrationEvent event) {
        Id channelId = idGenerator.convertIdFrom(event.salesChannelId());
        dispatch(new RefreshChannel(channelId, event.occurredAt()));
    }

    private void dispatch(ProjectDiscoveryProductCommand.ProjectionUpdate update) {
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }
}
