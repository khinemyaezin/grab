package com.grab.store.inventory.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.inventory.domain.enums.LocationType;

public record ListLocationsQuery(
        Boolean active,
        LocationType type
) implements Query<ListLocationsResult> {
}
