package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.ProductVariantSearchUseCase;
import com.catalog.application.model.read.ProductVariantSummaryQuery;
import com.catalog.application.model.read.ProductVariantSummaryResult;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductVariantSearchQueryHandler
        implements QueryHandler<ProductVariantSummaryQuery, Page<ProductVariantSummaryResult>> {

    private final ProductVariantSearchUseCase productVariantSearchUseCase;

    @Override
    @CatalogReadTransactional
    public Page<ProductVariantSummaryResult> handle(ProductVariantSummaryQuery query) {
        return productVariantSearchUseCase.execute(query);
    }

    @Override
    public Class<ProductVariantSummaryQuery> getQueryType() {
        return ProductVariantSummaryQuery.class;
    }
}
