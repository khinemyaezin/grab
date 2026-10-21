package com.grab.store.storefrontquery.internal.event;

import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.CatalogVariantChanged;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.ChannelEnabled;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.PriceSetChanged;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.ProductStatusChanged;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.Published;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RouteChanged;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.StockChanged;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.Unpublished;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.catalog.events.ProductPublishedToChannelIntegrationEvent;
import com.grab.store.catalog.events.ProductStatusChangedIntegrationEvent;
import com.grab.store.catalog.events.ProductUnpublishedFromChannelIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantAddedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantUpdatedIntegrationEvent;
import com.grab.store.inventory.events.LocationLinkedToChannelIntegrationEvent;
import com.grab.store.inventory.events.LocationUnlinkedFromChannelIntegrationEvent;
import com.grab.store.inventory.events.StockAdjustedIntegrationEvent;
import com.grab.store.inventory.events.StockReceivedIntegrationEvent;
import com.grab.store.inventory.events.StockReservedIntegrationEvent;
import com.grab.store.inventory.events.StockShippedIntegrationEvent;
import com.grab.store.pricing.events.PriceSetChangedIntegrationEvent;
import com.grab.store.saleschannel.events.SalesChannelDisabledIntegrationEvent;
import com.grab.store.saleschannel.events.SalesChannelEnabledIntegrationEvent;
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
        Id salesChannelId = idGenerator.convertIdFrom(event.salesChannelId());
        Published update = new Published(variantId, salesChannelId, event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onUnpublished(ProductUnpublishedFromChannelIntegrationEvent event) {
        Id variantId = idGenerator.convertIdFrom(event.variantId());
        Id salesChannelId = idGenerator.convertIdFrom(event.salesChannelId());
        Unpublished update = new Unpublished(variantId, salesChannelId, event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onVariantAdded(ProductVariantAddedIntegrationEvent event) {
        Id variantId = idGenerator.convertIdFrom(event.variantId());
        CatalogVariantChanged update = new CatalogVariantChanged(variantId, event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onVariantUpdated(ProductVariantUpdatedIntegrationEvent event) {
        Id variantId = idGenerator.convertIdFrom(event.variantId());
        CatalogVariantChanged update = new CatalogVariantChanged(variantId, event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onProductStatusChanged(ProductStatusChangedIntegrationEvent event) {
        Id productId = idGenerator.convertIdFrom(event.productId());
        ProductStatusChanged update = new ProductStatusChanged(productId, event.newStatus(), event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onStockReceived(StockReceivedIntegrationEvent event) {
        StockChanged update = new StockChanged(event.sku(), event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onStockReserved(StockReservedIntegrationEvent event) {
        StockChanged update = new StockChanged(event.sku(), event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onStockAdjusted(StockAdjustedIntegrationEvent event) {
        StockChanged update = new StockChanged(event.sku(), event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onStockShipped(StockShippedIntegrationEvent event) {
        StockChanged update = new StockChanged(event.sku(), event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onLocationLinked(LocationLinkedToChannelIntegrationEvent event) {
        Id locationId = idGenerator.convertIdFrom(event.locationId());
        Id salesChannelId = idGenerator.convertIdFrom(event.salesChannelId());
        RouteChanged update = new RouteChanged(locationId, salesChannelId, event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onLocationUnlinked(LocationUnlinkedFromChannelIntegrationEvent event) {
        Id locationId = idGenerator.convertIdFrom(event.locationId());
        Id salesChannelId = idGenerator.convertIdFrom(event.salesChannelId());
        RouteChanged update = new RouteChanged(locationId, salesChannelId, event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onPriceChanged(PriceSetChangedIntegrationEvent event) {
        Id priceSetId = idGenerator.convertIdFrom(event.priceSetId());
        PriceSetChanged update = new PriceSetChanged(priceSetId, event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onChannelEnabled(SalesChannelEnabledIntegrationEvent event) {
        Id salesChannelId = idGenerator.convertIdFrom(event.salesChannelId());
        ChannelEnabled update = new ChannelEnabled(salesChannelId, true, event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }

    @EventListener
    public void onChannelDisabled(SalesChannelDisabledIntegrationEvent event) {
        Id salesChannelId = idGenerator.convertIdFrom(event.salesChannelId());
        ChannelEnabled update = new ChannelEnabled(salesChannelId, false, event.occurredAt());
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }
}
