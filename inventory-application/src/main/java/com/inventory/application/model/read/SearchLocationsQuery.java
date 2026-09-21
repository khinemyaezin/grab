package com.inventory.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.framework.cqrs.query.PageableQueryRequest;
import com.inventory.domain.enums.LocationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record SearchLocationsQuery(
        Id merchantId,
        String query,
        LocationType type,
        Boolean active,
        Pageable pageable
) implements Query<Page<SearchLocationsResult>>, PageableQueryRequest {
}
