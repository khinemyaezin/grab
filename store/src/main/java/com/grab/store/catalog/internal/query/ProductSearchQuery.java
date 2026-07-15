package com.grab.store.catalog.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.store.shared.PageableQueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record ProductSearchQuery(
        String merchantId,
        String query,
        String variantStatus,
        String categoryId,
        String productStatus,
        Pageable pageable
) implements Query<Page<ProductSearchResult>>, PageableQueryRequest {
}
