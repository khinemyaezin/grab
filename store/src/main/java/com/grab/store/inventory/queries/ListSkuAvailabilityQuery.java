package com.grab.store.inventory.queries;

import com.grab.framework.cqrs.query.Query;

import java.util.List;

public record ListSkuAvailabilityQuery(
        List<String> skus
) implements Query<List<SkuAvailabilityResult>> {
}
