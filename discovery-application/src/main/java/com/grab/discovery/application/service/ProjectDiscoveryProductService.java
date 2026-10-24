package com.grab.discovery.application.service;

import com.grab.discovery.application.model.projection.DiscoveryProductProjection;
import com.grab.discovery.application.model.read.DiscoveryProductKey;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.ProjectionUpdate;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshChannel;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshLocation;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshProduct;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshProductChannel;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshSku;
import com.grab.discovery.application.model.write.ProjectDiscoveryProductCommand.RefreshVariant;
import com.grab.discovery.application.policy.DiscoveryProjectionPolicy;
import com.grab.discovery.application.port.inbound.ProjectDiscoveryProductUseCase;
import com.grab.discovery.application.port.outbound.CatalogPort;
import com.grab.discovery.application.port.outbound.DiscoveryProductRepository;
import com.grab.discovery.application.port.outbound.InventoryPort;
import com.grab.discovery.application.port.outbound.SalesChannelQueryPort;
import com.grab.framework.id.Id;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class ProjectDiscoveryProductService implements ProjectDiscoveryProductUseCase {
    private static final int TARGET_SCAN_BATCH_SIZE = 500;

    private final CatalogPort catalog;
    private final InventoryPort inventory;
    private final SalesChannelQueryPort salesChannels;
    private final DiscoveryProductRepository projections;
    private final DiscoveryProjectionPolicy policy;

    @Override
    public void execute(ProjectDiscoveryProductCommand command) {
        Instant watermark = command.update().occurredAt() == null ? Instant.now() : command.update().occurredAt();
        Map<String, TargetPair> targets = new LinkedHashMap<>();
        collectTargets(command.update(), targets);
        for (TargetPair target : targets.values()) {
            refresh(target, watermark);
        }
    }

    private void collectTargets(ProjectionUpdate update, Map<String, TargetPair> targets) {
        if (update instanceof RefreshVariant variantUpdate) {
            collectVariantTargets(variantUpdate, targets);
        } else if (update instanceof RefreshProduct productUpdate) {
            collectProductTargets(productUpdate.productId(), targets);
        } else if (update instanceof RefreshProductChannel productChannelUpdate) {
            addTarget(targets, productChannelUpdate.salesChannelId(), productChannelUpdate.productId());
        } else if (update instanceof RefreshSku skuUpdate) {
            for (DiscoveryProductProjection projection : projections.findBySku(skuUpdate.sku())) {
                addTarget(targets, projection.salesChannelId(), projection.productId());
            }
        } else if (update instanceof RefreshLocation locationUpdate) {
            if (locationUpdate.salesChannelId() == null) {
                collectAllProjectionTargets(targets);
            } else {
                collectChannelTargets(locationUpdate.salesChannelId(), targets);
            }
        } else if (update instanceof RefreshChannel channelUpdate) {
            collectChannelTargets(channelUpdate.salesChannelId(), targets);
        }
    }

    private void collectVariantTargets(RefreshVariant update, Map<String, TargetPair> targets) {
        List<DiscoveryProductProjection> existing = projections.findByVariant(update.variantId());
        for (DiscoveryProductProjection projection : existing) {
            if (update.salesChannelId() == null || sameId(update.salesChannelId(), projection.salesChannelId())) {
                addTarget(targets, projection.salesChannelId(), projection.productId());
            }
        }
        Optional<CatalogPort.VariantFacts> currentVariant = catalog.findVariant(update.variantId());
        if (currentVariant.isPresent()) {
            CatalogPort.VariantFacts variant = currentVariant.get();
            for (Id channelId : variant.salesChannelIds()) {
                boolean matchesRequestedChannel = update.salesChannelId() == null || sameId(update.salesChannelId(), channelId);
                if (matchesRequestedChannel) {
                    addTarget(targets, channelId, variant.productId());
                }
            }
            if (update.salesChannelId() != null) {
                addTarget(targets, update.salesChannelId(), variant.productId());
            }
        }
    }

    private void collectProductTargets(Id productId, Map<String, TargetPair> targets) {
        for (DiscoveryProductProjection projection : projections.findByProduct(productId)) {
            addTarget(targets, projection.salesChannelId(), productId);
        }
        Optional<CatalogPort.ProductFacts> source = catalog.findProduct(productId);
        if (source.isEmpty()) {
            return;
        }
        for (CatalogPort.VariantFacts variant : source.get().variants()) {
            for (Id channelId : variant.salesChannelIds()) {
                addTarget(targets, channelId, productId);
            }
        }
    }

    private void collectChannelTargets(Id channelId, Map<String, TargetPair> targets) {
        for (DiscoveryProductProjection projection : projections.findBySalesChannel(channelId)) {
            addTarget(targets, channelId, projection.productId());
        }
    }

    private void collectAllProjectionTargets(Map<String, TargetPair> targets) {
        Id lastProductId = null;
        Id lastChannelId = null;
        List<DiscoveryProductKey> batch;
        do {
            batch = projections.findKeysAfter(lastProductId, lastChannelId, TARGET_SCAN_BATCH_SIZE);
            for (DiscoveryProductKey key : batch) {
                addTarget(targets, key.salesChannelId(), key.productId());
            }
            if (!batch.isEmpty()) {
                DiscoveryProductKey lastKey = batch.getLast();
                lastProductId = lastKey.productId();
                lastChannelId = lastKey.salesChannelId();
            }
        } while (batch.size() == TARGET_SCAN_BATCH_SIZE);
    }

    private void refresh(TargetPair target, Instant watermark) {
        Optional<DiscoveryProductProjection> stored = projections.findBySalesChannelAndProduct(
                target.salesChannelId(),
                target.productId()
        );
        DiscoveryProductProjection previous = stored.orElse(null);
        if (policy.isOlderThanStored(watermark, previous)) {
            return;
        }
        Optional<CatalogPort.ProductFacts> source = catalog.findProduct(target.productId());
        if (source.isEmpty()) {
            invalidateIfUnpublished(target, previous, watermark);
            return;
        }
        CatalogPort.ProductFacts product = source.get();
        List<DiscoveryProductProjection.VariantFact> variants = currentPublishedVariants(
                target.salesChannelId(),
                product.merchantId(),
                product.variants()
        );
        if (variants.isEmpty()) {
            invalidateIfUnpublished(target, previous, watermark);
            return;
        }
        long version = previous == null ? DiscoveryProductProjection.NEW_PROJECTION_VERSION : previous.version();
        List<DiscoveryProductProjection.ProductOption> options = product.options().stream()
                .map(option -> new DiscoveryProductProjection.ProductOption(option.name(), option.values()))
                .toList();
        DiscoveryProductProjection projection = policy.compose(
                target.salesChannelId(),
                product.productId(),
                product.merchantId(),
                product.categoryId(),
                product.title(),
                product.slug(),
                product.description(),
                product.thumbnail(),
                product.status(),
                salesChannels.isEnabled(target.salesChannelId()),
                options,
                variants,
                maximumWatermark(previous, watermark),
                version
        );
        projections.save(projection);
    }

    private List<DiscoveryProductProjection.VariantFact> currentPublishedVariants(
            Id salesChannelId,
            Id merchantId,
            List<CatalogPort.VariantFacts> sourceVariants
    ) {
        List<DiscoveryProductProjection.VariantFact> variants = new ArrayList<>();
        for (CatalogPort.VariantFacts variant : sourceVariants) {
            boolean published = variant.salesChannelIds().stream().anyMatch(channel -> sameId(channel, salesChannelId));
            if (!published) {
                continue;
            }
            InventoryPort.Availability availability = variant.manageInventory()
                    ? inventory.available(variant.sku(), merchantId, salesChannelId)
                    : new InventoryPort.Availability(0, false);
            int quantity = variant.manageInventory() ? Math.max(availability.availableQty(), 0) : 0;
            variants.add(new DiscoveryProductProjection.VariantFact(
                    variant.variantId(),
                    variant.sku(),
                    variant.title(),
                    variant.status(),
                    variant.optionValues(),
                    quantity,
                    variant.manageInventory(),
                    availability.allowBackorder(),
                    quantity > 0
            ));
        }
        return List.copyOf(variants);
    }

    private void invalidateIfPresent(DiscoveryProductProjection previous, Instant watermark) {
        if (previous == null) {
            return;
        }
        if (policy.isInvalidated(previous) && !watermark.isAfter(previous.updatedAt())) {
            return;
        }
        DiscoveryProductProjection invalidated = policy.invalidate(previous, watermark);
        projections.save(invalidated);
    }

    private void invalidateIfUnpublished(TargetPair target, DiscoveryProductProjection previous, Instant watermark) {
        if (catalog.isPublishedInChannel(target.productId(), target.salesChannelId())) {
            throw new IllegalStateException("Catalog publication facts are inconsistent with product facts.");
        }
        invalidateIfPresent(previous, watermark);
    }

    private Instant maximumWatermark(DiscoveryProductProjection previous, Instant candidate) {
        if (previous != null && previous.updatedAt().isAfter(candidate)) {
            return previous.updatedAt();
        }
        return candidate;
    }

    private void addTarget(Map<String, TargetPair> targets, Id channelId, Id productId) {
        if (channelId == null || productId == null) {
            return;
        }
        String key = channelId.getValue() + ":" + productId.getValue();
        targets.putIfAbsent(key, new TargetPair(channelId, productId));
    }

    private boolean sameId(Id first, Id second) {
        return first != null && second != null && first.getValue().equals(second.getValue());
    }

    private record TargetPair(Id salesChannelId, Id productId) {
    }
}
