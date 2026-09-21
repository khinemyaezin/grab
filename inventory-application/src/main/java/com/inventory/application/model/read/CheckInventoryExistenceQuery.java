package com.inventory.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.List;

public record CheckInventoryExistenceQuery(
        Id merchantId,
        Id locationId,
        List<String> skus
) implements Query<CheckInventoryExistenceResult> {
}
