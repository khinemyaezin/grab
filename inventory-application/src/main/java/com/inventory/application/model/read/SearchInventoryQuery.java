package com.inventory.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.framework.cqrs.query.PageableQueryRequest;
import com.inventory.domain.enums.InventoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record SearchInventoryQuery(
        Id merchantId,
        String sku,
        Id locationId,
        InventoryStatus status,
        String variantId,
        Pageable pageable
) implements Query<Page<SearchInventoryResult>>, PageableQueryRequest {
}
