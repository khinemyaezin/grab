package com.catalog.application.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.cqrs.query.PageableQueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record ProductVariantSummaryQuery(
        String merchantId,
        String query,
        String variantStatus,
        String categoryId,
        String productStatus,
        Pageable pageable
) implements Query<Page<ProductVariantSummaryResult>>, PageableQueryRequest {
}
