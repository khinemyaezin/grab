package com.grab.store.catalog.queries;

import com.grab.framework.cqrs.query.Query;
import com.grab.store.shared.PageableQueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record SearchStorefrontProductsQuery(
        String query,
        String categoryId,
        String condition,
        Boolean featured,
        Pageable pageable
) implements Query<Page<StorefrontProductSearchResult>>, PageableQueryRequest {
}
