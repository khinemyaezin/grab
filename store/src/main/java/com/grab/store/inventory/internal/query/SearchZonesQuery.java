package com.grab.store.inventory.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.store.shared.PageableQueryRequest;
import com.inventory.domain.enums.ZoneType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record SearchZonesQuery(
        Id merchantId,
        Id locationId,
        String query,
        ZoneType type,
        Boolean active,
        Pageable pageable
) implements Query<Page<SearchZonesResult>>, PageableQueryRequest {
}
