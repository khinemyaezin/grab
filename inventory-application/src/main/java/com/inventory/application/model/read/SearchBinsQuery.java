package com.inventory.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.framework.cqrs.query.PageableQueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record SearchBinsQuery(
        Id merchantId,
        Id zoneId,
        String query,
        Boolean active,
        Pageable pageable
) implements Query<Page<SearchBinsResult>>, PageableQueryRequest {
}
