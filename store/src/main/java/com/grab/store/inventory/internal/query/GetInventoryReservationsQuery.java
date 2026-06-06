package com.grab.store.inventory.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.store.shared.PageableQueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record GetInventoryReservationsQuery(
        Id inventoryItemId,
        Pageable pageable
) implements Query<Page<GetInventoryReservationsResult>>, PageableQueryRequest {
}
