package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.queries.ListSkuAvailabilityQuery;
import com.grab.store.inventory.queries.SkuAvailabilityResult;
import com.inventory.domain.service.InventoryAllocationService;
import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.repository.jpa.ProductVariantViewJpaRepository;
import com.inventory.infrastructure.view.ProductView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListSkuAvailabilityQueryHandler
        implements QueryHandler<ListSkuAvailabilityQuery, List<SkuAvailabilityResult>> {

    private final InventoryAllocationService inventoryAllocationService;
    private final ProductVariantViewJpaRepository productVariantViewJpaRepository;

    @Override
    @InventoryReadTransactional
    public List<SkuAvailabilityResult> handle(ListSkuAvailabilityQuery query) {
        if (CollectionUtils.isEmpty(query.skus())) {
            return List.of();
        }
        Map<String, ProductView> activeBySku = productVariantViewJpaRepository.findAllBySkuIn(query.skus()).stream()
                .filter(view -> ProductVariantViewEntity.STATUS_ACTIVE.equals(view.getStatus()))
                .collect(Collectors.toMap(ProductView::getSku, Function.identity(), (first, ignored) -> first));

        Map<String, SkuAvailabilityResult> results = new LinkedHashMap<>();
        for (String sku : query.skus()) {
            if (sku == null || sku.isBlank() || results.containsKey(sku)) {
                continue;
            }
            results.put(sku, availabilityFor(sku, activeBySku.get(sku)));
        }
        return List.copyOf(results.values());
    }

    @Override
    public Class<ListSkuAvailabilityQuery> getQueryType() {
        return ListSkuAvailabilityQuery.class;
    }

    private SkuAvailabilityResult availabilityFor(String sku, ProductView view) {
        if (view != null && !view.isManageInventory()) {
            return new SkuAvailabilityResult(sku, 1, true);
        }
        int available = inventoryAllocationService.getAvailableForAllocation(sku);
        return new SkuAvailabilityResult(sku, available, available > 0);
    }
}
