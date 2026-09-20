package com.grab.store.storefrontquery.internal.event;

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
import com.grab.store.storefrontquery.internal.projection.BuyableOfferProjector;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BuyableOfferProjectionEventListener {
    private final BuyableOfferProjector projector;

    @EventListener
    public void onPublished(ProductPublishedToChannelIntegrationEvent event) {
        projector.onPublished(event.variantId(), event.salesChannelId(), event.occurredAt());
    }

    @EventListener
    public void onUnpublished(ProductUnpublishedFromChannelIntegrationEvent event) {
        projector.onUnpublished(event.variantId(), event.salesChannelId(), event.occurredAt());
    }

    @EventListener
    public void onVariantAdded(ProductVariantAddedIntegrationEvent event) {
        projector.onCatalogVariantChanged(event.variantId(), event.occurredAt());
    }

    @EventListener
    public void onVariantUpdated(ProductVariantUpdatedIntegrationEvent event) {
        projector.onCatalogVariantChanged(event.variantId(), event.occurredAt());
    }

    @EventListener
    public void onProductStatusChanged(ProductStatusChangedIntegrationEvent event) {
        projector.onProductStatusChanged(event.productId(), event.newStatus(), event.occurredAt());
    }

    @EventListener
    public void onStockReceived(StockReceivedIntegrationEvent event) {
        projector.onStockChanged(event.sku(), event.occurredAt());
    }

    @EventListener
    public void onStockReserved(StockReservedIntegrationEvent event) {
        projector.onStockChanged(event.sku(), event.occurredAt());
    }

    @EventListener
    public void onStockAdjusted(StockAdjustedIntegrationEvent event) {
        projector.onStockChanged(event.sku(), event.occurredAt());
    }

    @EventListener
    public void onStockShipped(StockShippedIntegrationEvent event) {
        projector.onStockChanged(event.sku(), event.occurredAt());
    }

    @EventListener
    public void onLocationLinked(LocationLinkedToChannelIntegrationEvent event) {
        projector.onRouteChanged(event.locationId(), event.salesChannelId(), event.occurredAt());
    }

    @EventListener
    public void onLocationUnlinked(LocationUnlinkedFromChannelIntegrationEvent event) {
        projector.onRouteChanged(event.locationId(), event.salesChannelId(), event.occurredAt());
    }

    @EventListener
    public void onPriceChanged(PriceSetChangedIntegrationEvent event) {
        projector.onPriceSetChanged(event.priceSetId(), event.occurredAt());
    }

    @EventListener
    public void onChannelEnabled(SalesChannelEnabledIntegrationEvent event) {
        projector.onChannelEnabled(event.salesChannelId(), true, event.occurredAt());
    }

    @EventListener
    public void onChannelDisabled(SalesChannelDisabledIntegrationEvent event) {
        projector.onChannelEnabled(event.salesChannelId(), false, event.occurredAt());
    }
}
