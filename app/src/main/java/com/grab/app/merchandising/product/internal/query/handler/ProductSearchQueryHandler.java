package com.grab.app.merchandising.product.internal.query.handler;

import com.catalog.application.model.read.ProductSearchQuery;
import com.catalog.application.model.read.ProductSearchResult;
import com.catalog.application.port.inbound.ProductSearchUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ProductSearchQueryHandler implements QueryHandler<ProductSearchQuery, Page<ProductSearchResult>> {

    private final ProductSearchUseCase productSearchUseCase;

    public ProductSearchQueryHandler(ProductSearchUseCase productSearchUseCase) {
        this.productSearchUseCase = productSearchUseCase;
    }

    @Override
    @CatalogReadTransactional
    public Page<ProductSearchResult> handle(ProductSearchQuery query) {
        return productSearchUseCase.execute(query);
    }

    @Override
    public Class<ProductSearchQuery> getQueryType() {
        return ProductSearchQuery.class;
    }
}
