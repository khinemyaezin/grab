package com.grab.store.inventory.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.store.shared.PageableQueryRequest;
import com.inventory.domain.enums.LocationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record ListLocationsQuery(
        Id merchantId,
        Boolean active,
        LocationType type,
        Pageable pageable
) implements Query<Page<ListLocationsResult>>, PageableQueryRequest{
}