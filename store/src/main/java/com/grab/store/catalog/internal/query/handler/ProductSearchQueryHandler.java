package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.ProductSearchUseCase;
import com.catalog.application.query.ProductSearchQuery;
import com.catalog.application.query.ProductSearchResult;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductSearchQueryHandler implements QueryHandler<ProductSearchQuery, Page<ProductSearchResult>> {

    private final ProductSearchUseCase productSearchUseCase;

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
