package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.service.InventoryAllocationService;
import com.grab.store.inventory.internal.query.GetAllocationAvailabilityQuery;
import com.grab.store.inventory.internal.query.GetAllocationAvailabilityResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAllocationAvailabilityQueryHandler
        implements QueryHandler<GetAllocationAvailabilityQuery, GetAllocationAvailabilityResult> {

    private final InventoryAllocationService inventoryAllocationService;

    @Override
    public GetAllocationAvailabilityResult handle(GetAllocationAvailabilityQuery query) {
        int available = inventoryAllocationService.getAvailableForAllocation(query.sku());
        int requested = query.quantity() == null ? 0 : query.quantity();
        boolean canAllocate = query.quantity() == null
                ? available > 0
                : inventoryAllocationService.canAllocate(query.sku(), query.quantity());
        return new GetAllocationAvailabilityResult(query.sku(), available, canAllocate, requested);
    }

    @Override
    public Class<GetAllocationAvailabilityQuery> getQueryType() {
        return GetAllocationAvailabilityQuery.class;
    }
}
