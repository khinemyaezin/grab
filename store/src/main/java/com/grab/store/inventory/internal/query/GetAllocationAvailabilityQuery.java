package com.grab.store.inventory.internal.query;

import com.grab.framework.cqrs.query.Query;

public record GetAllocationAvailabilityQuery(
        String sku,
        Integer quantity,
        String salesChannelId
) implements Query<GetAllocationAvailabilityResult> {
    public GetAllocationAvailabilityQuery(String sku, Integer quantity) {
        this(sku, quantity, null);
    }
}
