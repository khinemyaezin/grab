package com.grab.store.inventory.internal.query;

import com.grab.framework.cqrs.query.Query;

public record GetLocationByCodeQuery(
        String code
) implements Query<GetLocationResult> {
}
