package com.inventory.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.framework.cqrs.query.PageableQueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record GetInventoryMovementsQuery(
        Id inventoryItemId,
        Pageable pageable
) implements Query<Page<GetInventoryMovementsResult>>, PageableQueryRequest {
}
