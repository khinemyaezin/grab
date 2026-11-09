package com.grab.app.merchandising.catalog.internal.query.handler;

import com.catalog.application.model.read.ProductVariantSummaryQuery;
import com.catalog.application.model.read.ProductVariantSummaryResult;
import com.catalog.application.port.inbound.ProductVariantSearchUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ProductVariantSearchQueryHandler implements QueryHandler<ProductVariantSummaryQuery, Page<ProductVariantSummaryResult>> {

    private final ProductVariantSearchUseCase productVariantSearchUseCase;

    public ProductVariantSearchQueryHandler(ProductVariantSearchUseCase productVariantSearchUseCase) {
        this.productVariantSearchUseCase = productVariantSearchUseCase;
    }

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
