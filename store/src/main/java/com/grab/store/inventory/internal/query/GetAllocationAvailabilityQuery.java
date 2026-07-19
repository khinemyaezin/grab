package com.grab.store.inventory.internal.query;

import com.grab.framework.cqrs.query.Query;

public record GetAllocationAvailabilityQuery(
        String sku,
        Integer quantity
) implements Query<GetAllocationAvailabilityResult> {
}
