package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.service.InventoryAllocationService;
import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.repository.jpa.ProductVariantViewJpaRepository;
import com.grab.store.inventory.internal.query.GetAllocationAvailabilityQuery;
import com.grab.store.inventory.internal.query.GetAllocationAvailabilityResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAllocationAvailabilityQueryHandler
        implements QueryHandler<GetAllocationAvailabilityQuery, GetAllocationAvailabilityResult> {

    private final InventoryAllocationService inventoryAllocationService;
    private final ProductVariantViewJpaRepository productVariantViewJpaRepository;

    @Override
    public GetAllocationAvailabilityResult handle(GetAllocationAvailabilityQuery query) {
        int requested = query.quantity() == null ? 0 : query.quantity();
        if (isUntracked(query.sku())) {
            int available = requested > 0 ? requested : 1;
            return new GetAllocationAvailabilityResult(query.sku(), available, true, requested);
        }
        int available = inventoryAllocationService.getAvailableForAllocation(query.sku());
        boolean canAllocate = query.quantity() == null
                ? available > 0
                : inventoryAllocationService.canAllocate(query.sku(), query.quantity());
        return new GetAllocationAvailabilityResult(query.sku(), available, canAllocate, requested);
    }

    @Override
    public Class<GetAllocationAvailabilityQuery> getQueryType() {
        return GetAllocationAvailabilityQuery.class;
    }

    private boolean isUntracked(String sku) {
        return productVariantViewJpaRepository.findBySkuAndStatus(sku, ProductVariantViewEntity.STATUS_ACTIVE)
                .map(view -> !view.isManageInventory())
                .orElse(false);
    }
}
