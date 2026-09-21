package com.inventory.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.adapter.persistence.entity.InventoryItemEntity;
import com.inventory.adapter.persistence.entity.LocationEntity;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.adapter.persistence.repository.jpa.ChannelFulfillmentRouteJpaRepository;
import com.inventory.adapter.persistence.repository.jpa.InventoryItemJpaRepository;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.adapter.persistence.repository.jpa.LocationJpaRepository;
import com.inventory.application.model.read.InventorySearchCriteria;
import com.inventory.adapter.persistence.specification.jpa.InventorySearchSpecification;
import com.inventory.adapter.persistence.specification.jpa.InventorySummarySpecification;
import com.inventory.application.model.read.CountBucketView;
import com.inventory.application.model.read.InventoryExistenceView;
import com.inventory.application.model.read.InventoryItemView;
import com.inventory.application.model.read.InventoryQuantityTotalsView;
import com.inventory.application.model.read.InventoryStatusBreakdownView;
import com.inventory.application.model.read.InventoryStockHealthBreakdownView;
import com.inventory.application.model.read.InventorySummaryAggregationView;
import com.inventory.application.model.read.InventorySummaryScopeView;
import com.inventory.application.model.read.InventorySummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class InventoryQueryAdapter implements InventoryQueryPort {

    private static final String INVENTORY_ITEM_RESOURCE = "InventoryItem";
    private static final String ROUTE_RESOURCE = "ChannelFulfillmentRoute";

    private final InventorySearchSpecification searchSpecification;
    private final InventorySummarySpecification summarySpecification;
    private final InventoryItemJpaRepository jpaRepository;
    private final LocationJpaRepository locationJpaRepository;
    private final ChannelFulfillmentRouteJpaRepository channelFulfillmentRouteJpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public Page<InventoryItemView> search(InventorySearchCriteria criteria, Pageable pageable) {
        return executor.query(INVENTORY_ITEM_RESOURCE, () -> searchSpecification.search(criteria, pageable));
    }

    @Override
    public List<InventoryExistenceView> findExistenceByMerchantLocationAndSkus(
            String merchantId,
            String locationId,
            Collection<String> skus
    ) {
        return executor.query(INVENTORY_ITEM_RESOURCE, () -> {
            List<InventoryItemEntity> entities = jpaRepository.findAllByMerchantIdAndLocationIdAndSkuIn(
                    merchantId,
                    locationId,
                    skus
            );
            return entities.stream()
                    .map(entity -> new InventoryExistenceView(entity.getUuid(), entity.getSku()))
                    .toList();
        });
    }

    @Override
    public Optional<InventoryItemView> findById(String inventoryItemId) {
        return executor.query(INVENTORY_ITEM_RESOURCE, () -> searchSpecification.findById(inventoryItemId));
    }

    @Override
    public boolean existsActiveRoute(String merchantId, String salesChannelId) {
        return executor.query(ROUTE_RESOURCE, () ->
                channelFulfillmentRouteJpaRepository.existsActiveForMerchantAndChannel(merchantId, salesChannelId));
    }

    @Override
    public int sumAvailableForAllocation(String sku, String salesChannelId) {
        return executor.query(INVENTORY_ITEM_RESOURCE, () ->
                jpaRepository.findAllBySku(sku).stream()
                        .filter(item -> item.getStatus() == InventoryStatus.ACTIVE)
                        .filter(item -> availableQuantity(item) > 0)
                        .filter(item -> isActiveLocation(item.getLocationId()))
                        .filter(item -> salesChannelId == null || routeExists(item.getLocationId(), salesChannelId))
                        .mapToInt(this::availableQuantity)
                        .sum());
    }

    @Override
    public List<String> findSkusByLocation(String locationId) {
        return executor.query(INVENTORY_ITEM_RESOURCE, () ->
                jpaRepository.findAllByLocationId(locationId).stream()
                        .map(InventoryItemEntity::getSku)
                        .distinct()
                        .toList());
    }

    @Override
    public List<InventoryItemView> findReorderCandidates(String merchantId, String locationId) {
        return executor.query(INVENTORY_ITEM_RESOURCE, () -> {
            List<InventoryItemEntity> entities = locationId == null
                    ? jpaRepository.findAllByMerchantId(merchantId)
                    : jpaRepository.findAllByLocationId(locationId);
            return entities.stream()
                    .filter(item -> item.getStatus() == InventoryStatus.ACTIVE)
                    .map(this::toItemView)
                    .toList();
        });
    }

    private InventoryItemView toItemView(InventoryItemEntity item) {
        LocationEntity location = locationJpaRepository.findByUuid(item.getLocationId()).orElse(null);
        return new InventoryItemView(
                item.getUuid(),
                item.getSku(),
                item.getMerchantId(),
                item.getProductVariantId(),
                item.getLocationId(),
                location == null ? null : location.getCode(),
                location == null ? null : location.getName(),
                item.getOnHand(),
                item.getReserved(),
                item.getInTransit(),
                item.getDamaged(),
                item.getSafetyStock(),
                item.getReorderPoint(),
                item.getReorderQuantity(),
                item.getMaxStock(),
                item.getStatus(),
                item.getLastUpdated()
        );
    }

    private int availableQuantity(InventoryItemEntity item) {
        return item.getOnHand() - item.getReserved() - item.getDamaged();
    }

    private boolean isActiveLocation(String locationUuid) {
        return locationJpaRepository.findByUuid(locationUuid).map(LocationEntity::isActive).orElse(false);
    }

    private boolean routeExists(String locationUuid, String salesChannelId) {
        return locationJpaRepository.findByUuid(locationUuid)
                .map(location -> channelFulfillmentRouteJpaRepository.existsByLocationIdAndSalesChannelId(
                        location.getId(),
                        salesChannelId
                ))
                .orElse(false);
    }

    @Override
    public InventorySummaryView summarize(String merchantId, String locationId) {
        return executor.query(INVENTORY_ITEM_RESOURCE, () -> {
            InventorySummaryAggregationView aggregation = summarySpecification.aggregate(merchantId, locationId);
            InventorySummaryScopeView scope = resolveScope(merchantId, locationId);
            return toSummaryView(scope, aggregation);
        });
    }

    private InventorySummaryScopeView resolveScope(String merchantId, String locationId) {
        if (!StringUtils.hasText(locationId)) {
            return new InventorySummaryScopeView(merchantId, null, null, null);
        }
        String locationCode = null;
        String locationName = null;
        LocationEntity location = locationJpaRepository.findByUuid(locationId).orElse(null);
        if (location != null) {
            locationCode = location.getCode();
            locationName = location.getName();
        }
        return new InventorySummaryScopeView(merchantId, locationId, locationCode, locationName);
    }

    private static InventorySummaryView toSummaryView(
            InventorySummaryScopeView scope,
            InventorySummaryAggregationView aggregation
    ) {
        InventoryStatusBreakdownView status = new InventoryStatusBreakdownView(
                new CountBucketView(aggregation.activeCount()),
                new CountBucketView(aggregation.statusOutOfStockCount()),
                new CountBucketView(aggregation.suspendedCount()),
                new CountBucketView(aggregation.discontinuedCount())
        );
        InventoryStockHealthBreakdownView health = new InventoryStockHealthBreakdownView(
                aggregation.healthEligibleItems(),
                new CountBucketView(aggregation.healthInStock()),
                new CountBucketView(aggregation.healthLowStock()),
                new CountBucketView(aggregation.healthOutOfStock()),
                new CountBucketView(aggregation.healthCritical())
        );
        InventoryQuantityTotalsView quantities = new InventoryQuantityTotalsView(
                aggregation.onHand(),
                aggregation.reserved(),
                aggregation.inTransit(),
                aggregation.damaged(),
                aggregation.available()
        );
        return new InventorySummaryView(
                scope,
                aggregation.totalItems(),
                status,
                health,
                quantities
        );
    }
}
