package com.grab.store.inventory.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.List;

public record CheckInventoryExistenceQuery(
        Id merchantId,
        Id locationId,
        List<String> skus
) implements Query<CheckInventoryExistenceResult> {
}
