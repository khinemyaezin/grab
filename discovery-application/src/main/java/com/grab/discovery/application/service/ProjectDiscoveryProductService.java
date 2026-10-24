package com.grab.discovery.application.service;

import com.grab.discovery.application.model.projection.DiscoveryProductProjection;
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
import java.util.Comparator;
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
        List<TargetPair> orderedTargets = targets.values().stream()
                .sorted(Comparator.comparing(TargetPair::salesChannelId, this::compareIds)
                        .thenComparing(TargetPair::productId, this::compareIds))
                .toList();
        for (TargetPair target : orderedTargets) {
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
            collectStoredSkuTargets(skuUpdate.sku(), targets);
            collectCurrentSkuTargets(skuUpdate.sku(), targets);
        } else if (update instanceof RefreshLocation locationUpdate) {
            collectLocationTargets(locationUpdate, targets);
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
        List<DiscoveryProductProjection> storedProjections = projections.findBySalesChannel(channelId);
        for (DiscoveryProductProjection projection : safeList(storedProjections)) {
            addTarget(targets, channelId, projection.productId());
        }
        Id lastProductId = null;
        List<CatalogPort.ProductChannelPair> batch;
        do {
            List<CatalogPort.ProductChannelPair> currentBatch = catalog.findPublishedProductChannelsForSalesChannelAfter(
                    channelId,
                    lastProductId,
                    TARGET_SCAN_BATCH_SIZE
            );
            batch = safeList(currentBatch);
            addTargets(targets, batch);
            if (!batch.isEmpty()) {
                lastProductId = batch.getLast().productId();
            }
        } while (batch.size() == TARGET_SCAN_BATCH_SIZE);
    }

    private void collectStoredSkuTargets(String sku, Map<String, TargetPair> targets) {
        List<DiscoveryProductProjection> storedProjections = projections.findBySku(sku);
        if (storedProjections == null) {
            return;
        }
        for (DiscoveryProductProjection projection : storedProjections) {
            addTarget(targets, projection.salesChannelId(), projection.productId());
        }
    }

    private void collectCurrentSkuTargets(String sku, Map<String, TargetPair> targets) {
        List<CatalogPort.ProductChannelPair> currentPairs = catalog.findPublishedProductChannelsBySku(sku);
        addTargets(targets, currentPairs);
    }

    private void collectLocationTargets(RefreshLocation update, Map<String, TargetPair> targets) {
        List<String> skus = inventory.skusAtLocation(update.locationId());
        if (skus == null) {
            return;
        }
        for (String sku : skus) {
            collectLocationSkuTargets(sku, update.salesChannelId(), targets);
        }
    }

    private void collectLocationSkuTargets(
            String sku,
            Id requestedChannelId,
            Map<String, TargetPair> targets
    ) {
        List<DiscoveryProductProjection> storedProjections = projections.findBySku(sku);
        if (storedProjections != null) {
            for (DiscoveryProductProjection projection : storedProjections) {
                if (requestedChannelId == null || sameId(requestedChannelId, projection.salesChannelId())) {
                    addTarget(targets, projection.salesChannelId(), projection.productId());
                }
            }
        }
        List<CatalogPort.ProductChannelPair> currentPairs = catalog.findPublishedProductChannelsBySku(sku);
        for (CatalogPort.ProductChannelPair pair : safeList(currentPairs)) {
            if (requestedChannelId == null || sameId(requestedChannelId, pair.salesChannelId())) {
                addTarget(targets, pair.salesChannelId(), pair.productId());
            }
        }
    }

    private void addTargets(
            Map<String, TargetPair> targets,
            List<CatalogPort.ProductChannelPair> pairs
    ) {
        for (CatalogPort.ProductChannelPair pair : safeList(pairs)) {
            addTarget(targets, pair.salesChannelId(), pair.productId());
        }
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
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
                .sorted(Comparator.comparing(
                        CatalogPort.ProductOption::name,
                        Comparator.nullsLast(String::compareTo)
                ))
                .map(option -> new DiscoveryProductProjection.ProductOption(
                        option.name(),
                        option.values().stream().sorted().toList()
                ))
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
        List<CatalogPort.VariantFacts> orderedVariants = sourceVariants.stream()
                .sorted(Comparator.comparing(CatalogPort.VariantFacts::variantId, this::compareIds))
                .toList();
        for (CatalogPort.VariantFacts variant : orderedVariants) {
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
                    orderedOptionValues(variant.optionValues()),
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

    private int compareIds(Id first, Id second) {
        if (first == second) {
            return 0;
        }
        if (first == null) {
            return 1;
        }
        if (second == null) {
            return -1;
        }
        return first.getValue().compareTo(second.getValue());
    }

    private Map<String, String> orderedOptionValues(Map<String, String> optionValues) {
        if (optionValues == null || optionValues.isEmpty()) {
            return Map.of();
        }
        Map<String, String> orderedValues = new LinkedHashMap<>();
        optionValues.entrySet().stream()
                .sorted((first, second) -> compareStrings(first.getKey(), second.getKey()))
                .forEach(entry -> orderedValues.put(entry.getKey(), entry.getValue()));
        return orderedValues;
    }

    private int compareStrings(String first, String second) {
        if (first == second) {
            return 0;
        }
        if (first == null) {
            return 1;
        }
        if (second == null) {
            return -1;
        }
        return first.compareTo(second);
    }

    private record TargetPair(Id salesChannelId, Id productId) {
    }
}
