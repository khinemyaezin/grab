package com.grab.store.storefrontquery.internal.projection;

import com.grab.store.catalog.query.CatalogBuyabilityQueryPort;
import com.grab.store.inventory.query.InventoryAvailabilityQueryPort;
import com.grab.store.pricing.query.PricingQuoteQueryPort;
import com.grab.store.saleschannel.query.SalesChannelQueryPort;
import com.grab.store.storefrontquery.internal.config.StorefrontQueryTransactional;
import com.storefrontquery.infrastructure.entity.BuyableOfferEntity;
import com.storefrontquery.infrastructure.repository.jpa.BuyableOfferJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BuyableOfferProjector {
    private final BuyableOfferJpaRepository offers;
    private final CatalogBuyabilityQueryPort catalog;
    private final PricingQuoteQueryPort pricing;
    private final InventoryAvailabilityQueryPort inventory;
    private final SalesChannelQueryPort salesChannels;

    @Value("${storefrontquery.display-currency:MMK}")
    private String displayCurrency;

    @StorefrontQueryTransactional
    public void onPublished(String variantId, String salesChannelId, Instant occurredAt) {
        upsert(variantId, salesChannelId, true, occurredAt);
    }

    @StorefrontQueryTransactional
    public void onUnpublished(String variantId, String salesChannelId, Instant occurredAt) {
        BuyableOfferEntity entity = offers.findBySalesChannelIdAndVariantId(salesChannelId, variantId)
                .orElseGet(() -> newRow(variantId, salesChannelId));
        if (!entity.shouldApply(occurredAt)) {
            return;
        }
        entity.setPublished(false);
        entity.setUpdatedAt(occurredAt == null ? Instant.now() : occurredAt);
        entity.recomputeBuyable();
        offers.save(entity);
    }

    @StorefrontQueryTransactional
    public void onCatalogVariantChanged(String variantId, Instant occurredAt) {
        List<String> channels = catalog.salesChannelIdsForVariant(variantId);
        if (channels.isEmpty()) {
            for (BuyableOfferEntity existing : offers.findByVariantId(variantId)) {
                applyCatalogSlice(existing, occurredAt);
                offers.save(existing);
            }
            return;
        }
        for (String channelId : channels) {
            upsert(variantId, channelId, true, occurredAt);
        }
    }

    @StorefrontQueryTransactional
    public void onProductStatusChanged(String productId, String newStatus, Instant occurredAt) {
        for (String variantId : catalog.variantIdsForProduct(productId)) {
            for (BuyableOfferEntity entity : offers.findByVariantId(variantId)) {
                if (!entity.shouldApply(occurredAt)) {
                    continue;
                }
                entity.setProductStatus(newStatus);
                entity.setUpdatedAt(occurredAt == null ? Instant.now() : occurredAt);
                entity.recomputeBuyable();
                offers.save(entity);
            }
        }
    }

    @StorefrontQueryTransactional
    public void onStockChanged(String sku, Instant occurredAt) {
        for (BuyableOfferEntity entity : offers.findBySku(sku)) {
            refreshStockAndPrice(entity, occurredAt);
            offers.save(entity);
        }
    }

    @StorefrontQueryTransactional
    public void onRouteChanged(String locationId, String salesChannelId, Instant occurredAt) {
        for (String sku : inventory.skusAtLocation(locationId)) {
            for (BuyableOfferEntity entity : offers.findBySku(sku)) {
                if (!entity.getSalesChannelId().equals(salesChannelId)) {
                    continue;
                }
                refreshStockAndPrice(entity, occurredAt);
                offers.save(entity);
            }
        }
    }

    @StorefrontQueryTransactional
    public void onPriceSetChanged(String priceSetId, Instant occurredAt) {
        for (String variantId : pricing.variantIdsForPriceSet(priceSetId)) {
            for (BuyableOfferEntity entity : offers.findByVariantId(variantId)) {
                refreshStockAndPrice(entity, occurredAt);
                offers.save(entity);
            }
        }
    }

    @StorefrontQueryTransactional
    public void onChannelEnabled(String salesChannelId, boolean enabled, Instant occurredAt) {
        for (BuyableOfferEntity entity : offers.findBySalesChannelId(salesChannelId)) {
            if (!entity.shouldApply(occurredAt)) {
                continue;
            }
            entity.setChannelEnabled(enabled);
            entity.setUpdatedAt(occurredAt == null ? Instant.now() : occurredAt);
            entity.recomputeBuyable();
            offers.save(entity);
        }
    }

    @StorefrontQueryTransactional
    public void rebuild() {
        Instant now = Instant.now();
        for (CatalogBuyabilityQueryPort.PublicationSlice publication : catalog.listPublications()) {
            upsert(publication.variantId(), publication.salesChannelId(), true, now);
        }
    }

    private void upsert(String variantId, String salesChannelId, boolean published, Instant occurredAt) {
        BuyableOfferEntity entity = offers.findBySalesChannelIdAndVariantId(salesChannelId, variantId)
                .orElseGet(() -> newRow(variantId, salesChannelId));
        if (!entity.shouldApply(occurredAt)) {
            return;
        }
        entity.setPublished(published);
        applyCatalogSlice(entity, occurredAt);
        refreshStockAndPrice(entity, occurredAt);
        offers.save(entity);
    }

    private void applyCatalogSlice(BuyableOfferEntity entity, Instant occurredAt) {
        catalog.findVariant(entity.getVariantId()).ifPresent(slice -> {
            entity.setProductId(slice.productId());
            entity.setSellerId(slice.sellerId());
            entity.setSku(slice.sku());
            entity.setTitle(slice.title());
            entity.setSlug(slice.slug());
            entity.setProductStatus(slice.productStatus());
            entity.setUntracked(slice.untracked());
            entity.setMedia(slice.media());
        });
        entity.setChannelEnabled(salesChannels.isEnabled(entity.getSalesChannelId()));
        entity.setUpdatedAt(occurredAt == null ? Instant.now() : occurredAt);
        entity.recomputeBuyable();
    }

    private void refreshStockAndPrice(BuyableOfferEntity entity, Instant occurredAt) {
        if (!entity.shouldApply(occurredAt)) {
            return;
        }
        if (entity.getSku() != null) {
            InventoryAvailabilityQueryPort.Availability availability =
                    inventory.available(entity.getSku(), entity.getSalesChannelId());
            entity.setUntracked(availability.untracked());
            entity.setAvailableQty(availability.availableQty());
        }
        pricing.quote(entity.getVariantId(), currency(), 1, entity.getSalesChannelId())
                .ifPresent(quoted -> {
                    entity.setAmount(quoted.amount());
                    entity.setCurrency(quoted.currencyCode());
                });
        entity.setChannelEnabled(salesChannels.isEnabled(entity.getSalesChannelId()));
        entity.setUpdatedAt(occurredAt == null ? Instant.now() : occurredAt);
        entity.recomputeBuyable();
    }

    private BuyableOfferEntity newRow(String variantId, String salesChannelId) {
        BuyableOfferEntity entity = new BuyableOfferEntity();
        entity.setVariantId(variantId);
        entity.setSalesChannelId(salesChannelId);
        entity.setPublished(false);
        entity.setAvailableQty(0);
        entity.setChannelEnabled(true);
        entity.setUpdatedAt(Instant.EPOCH);
        return entity;
    }

    private String currency() {
        return displayCurrency == null || displayCurrency.isBlank() ? "MMK" : displayCurrency;
    }
}
