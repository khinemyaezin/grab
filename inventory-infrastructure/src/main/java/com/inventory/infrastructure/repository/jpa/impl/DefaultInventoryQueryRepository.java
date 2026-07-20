package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import com.inventory.infrastructure.repository.jpa.InventoryQueryRepository;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.specification.jpa.InventorySearchCriteria;
import com.inventory.infrastructure.specification.jpa.InventorySearchSpecification;
import com.inventory.infrastructure.specification.jpa.InventorySummarySpecification;
import com.inventory.infrastructure.view.CountBucketView;
import com.inventory.infrastructure.view.InventoryExistenceView;
import com.inventory.infrastructure.view.InventoryItemView;
import com.inventory.infrastructure.view.InventoryQuantityTotalsView;
import com.inventory.infrastructure.view.InventoryStatusBreakdownView;
import com.inventory.infrastructure.view.InventoryStockHealthBreakdownView;
import com.inventory.infrastructure.view.InventorySummaryAggregationView;
import com.inventory.infrastructure.view.InventorySummaryScopeView;
import com.inventory.infrastructure.view.InventorySummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class DefaultInventoryQueryRepository implements InventoryQueryRepository {

    private static final String INVENTORY_ITEM_RESOURCE = "InventoryItem";

    private final InventorySearchSpecification searchSpecification;
    private final InventorySummarySpecification summarySpecification;
    private final InventoryItemJpaRepository jpaRepository;
    private final LocationJpaRepository locationJpaRepository;
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
