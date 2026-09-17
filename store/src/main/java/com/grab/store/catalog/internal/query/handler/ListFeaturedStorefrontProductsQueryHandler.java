package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.queries.ListFeaturedStorefrontProductsQuery;
import com.grab.store.catalog.queries.StorefrontProductSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListFeaturedStorefrontProductsQueryHandler
        implements QueryHandler<ListFeaturedStorefrontProductsQuery, Page<StorefrontProductSearchResult>> {

    private final StorefrontProductSearchMapper mapper;

    @Override
    @CatalogReadTransactional
    public Page<StorefrontProductSearchResult> handle(ListFeaturedStorefrontProductsQuery query) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .featured(true)
                .storefrontVisible(true)
                .build();
        return mapper.search(criteria, query.pageable());
    }

    @Override
    public Class<ListFeaturedStorefrontProductsQuery> getQueryType() {
        return ListFeaturedStorefrontProductsQuery.class;
    }
}
