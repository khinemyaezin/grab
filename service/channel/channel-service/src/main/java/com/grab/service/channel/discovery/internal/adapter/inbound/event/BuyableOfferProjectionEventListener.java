package com.grab.service.channel.discovery.internal.adapter.inbound.event;

import com.grab.service.contract.event.merchandising.catalog.ProductDeletedIntegrationEvent;
import com.grab.service.contract.event.merchandising.catalog.ProductPresentationChangedIntegrationEvent;
import com.grab.service.contract.event.merchandising.catalog.ProductPublishedToChannelIntegrationEvent;
import com.grab.service.contract.event.merchandising.catalog.ProductStatusChangedIntegrationEvent;
import com.grab.service.contract.event.merchandising.catalog.ProductUnpublishedFromChannelIntegrationEvent;
import com.grab.service.contract.event.merchandising.catalog.ProductVariantAddedIntegrationEvent;
import com.grab.service.contract.event.merchandising.catalog.ProductVariantDeletedIntegrationEvent;
import com.grab.service.contract.event.merchandising.catalog.ProductVariantRestoredIntegrationEvent;
import com.grab.service.contract.event.merchandising.catalog.ProductVariantUpdatedIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.InventoryStatusChangedIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.LocationLinkedToChannelIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.LocationStatusChangedIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.LocationUnlinkedFromChannelIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.StockAdjustedIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.StockReceivedIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.StockReservedIntegrationEvent;
import com.grab.service.contract.event.merchandising.inventory.StockShippedIntegrationEvent;
import com.grab.service.contract.event.channel.saleschannel.SalesChannelDisabledIntegrationEvent;
import com.grab.service.contract.event.channel.saleschannel.SalesChannelEnabledIntegrationEvent;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshChannel;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshLocation;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshProduct;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshSku;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshVariant;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.outbox.infrastructure.inbox.AbstractIntegrationEventListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class BuyableOfferProjectionEventListener extends AbstractIntegrationEventListener {

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    public BuyableOfferProjectionEventListener(
            @Qualifier("channelCommandBus") CommandBus commandBus,
            IdGenerator idGenerator,
            @Qualifier("discoveryTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("discoveryProcessedEventRepository") ProcessedEventRepository processedEvents
    ) {
        super(transactionManager, processedEvents);
        this.commandBus = commandBus;
        this.idGenerator = idGenerator;
    }

    @EventListener
    public void onPublished(ProductPublishedToChannelIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id variantId = idGenerator.convertIdFrom(event.variantId());
            Id channelId = idGenerator.convertIdFrom(event.salesChannelId());
            dispatch(new RefreshVariant(variantId, channelId, event.occurredAt()));
        });
    }

    @EventListener
    public void onUnpublished(ProductUnpublishedFromChannelIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id variantId = idGenerator.convertIdFrom(event.variantId());
            Id channelId = idGenerator.convertIdFrom(event.salesChannelId());
            dispatch(new RefreshVariant(variantId, channelId, event.occurredAt()));
        });
    }

    @EventListener
    public void onVariantAdded(ProductVariantAddedIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id productId = idGenerator.convertIdFrom(event.productId());
            dispatch(new RefreshProduct(productId, event.occurredAt()));
        });
    }

    @EventListener
    public void onVariantUpdated(ProductVariantUpdatedIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id productId = idGenerator.convertIdFrom(event.productId());
            dispatch(new RefreshProduct(productId, event.occurredAt()));
        });
    }

    @EventListener
    public void onVariantDeleted(ProductVariantDeletedIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id productId = idGenerator.convertIdFrom(event.productId());
            dispatch(new RefreshProduct(productId, event.occurredAt()));
        });
    }

    @EventListener
    public void onVariantRestored(ProductVariantRestoredIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id productId = idGenerator.convertIdFrom(event.productId());
            dispatch(new RefreshProduct(productId, event.occurredAt()));
        });
    }

    @EventListener
    public void onProductDeleted(ProductDeletedIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id productId = idGenerator.convertIdFrom(event.productId());
            dispatch(new RefreshProduct(productId, event.occurredAt()));
        });
    }

    @EventListener
    public void onProductPresentationChanged(ProductPresentationChangedIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id productId = idGenerator.convertIdFrom(event.productId());
            dispatch(new RefreshProduct(productId, event.occurredAt()));
        });
    }

    @EventListener
    public void onProductStatusChanged(ProductStatusChangedIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id productId = idGenerator.convertIdFrom(event.productId());
            dispatch(new RefreshProduct(productId, event.occurredAt()));
        });
    }

    @EventListener
    public void onStockReceived(StockReceivedIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            dispatch(new RefreshSku(event.sku(), event.occurredAt()));
        });
    }

    @EventListener
    public void onStockReserved(StockReservedIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            dispatch(new RefreshSku(event.sku(), event.occurredAt()));
        });
    }

    @EventListener
    public void onStockAdjusted(StockAdjustedIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            dispatch(new RefreshSku(event.sku(), event.occurredAt()));
        });
    }

    @EventListener
    public void onStockShipped(StockShippedIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            dispatch(new RefreshSku(event.sku(), event.occurredAt()));
        });
    }

    @EventListener
    public void onInventoryStatusChanged(InventoryStatusChangedIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            dispatch(new RefreshSku(event.sku(), event.occurredAt()));
        });
    }

    @EventListener
    public void onLocationStatusChanged(LocationStatusChangedIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id locationId = idGenerator.convertIdFrom(event.locationId());
            dispatch(new RefreshLocation(locationId, null, event.occurredAt()));
        });
    }

    @EventListener
    public void onLocationLinked(LocationLinkedToChannelIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id locationId = idGenerator.convertIdFrom(event.locationId());
            Id channelId = idGenerator.convertIdFrom(event.salesChannelId());
            dispatch(new RefreshLocation(locationId, channelId, event.occurredAt()));
        });
    }

    @EventListener
    public void onLocationUnlinked(LocationUnlinkedFromChannelIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id locationId = idGenerator.convertIdFrom(event.locationId());
            Id channelId = idGenerator.convertIdFrom(event.salesChannelId());
            dispatch(new RefreshLocation(locationId, channelId, event.occurredAt()));
        });
    }

    @EventListener
    public void onChannelEnabled(SalesChannelEnabledIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id channelId = idGenerator.convertIdFrom(event.salesChannelId());
            dispatch(new RefreshChannel(channelId, event.occurredAt()));
        });
    }

    @EventListener
    public void onChannelDisabled(SalesChannelDisabledIntegrationEvent event) {
        processDelivery("discovery.project-buyable-offer", () -> {
            Id channelId = idGenerator.convertIdFrom(event.salesChannelId());
            dispatch(new RefreshChannel(channelId, event.occurredAt()));
        });
    }

    private void dispatch(ProjectDiscoveryProductCommand.ProjectionUpdate update) {
        ProjectDiscoveryProductCommand command = new ProjectDiscoveryProductCommand(update);
        commandBus.dispatch(command);
    }
}
