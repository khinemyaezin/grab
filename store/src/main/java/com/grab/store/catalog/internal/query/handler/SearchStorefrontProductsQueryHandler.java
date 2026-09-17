package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.queries.SearchStorefrontProductsQuery;
import com.grab.store.catalog.queries.StorefrontProductSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchStorefrontProductsQueryHandler
        implements QueryHandler<SearchStorefrontProductsQuery, Page<StorefrontProductSearchResult>> {

    private final StorefrontProductSearchMapper mapper;

    @Override
    @CatalogReadTransactional
    public Page<StorefrontProductSearchResult> handle(SearchStorefrontProductsQuery query) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .query(query.query())
                .categoryId(query.categoryId())
                .condition(query.condition())
                .featured(query.featured())
                .storefrontVisible(true)
                .build();
        return mapper.search(criteria, query.pageable());
    }

    @Override
    public Class<SearchStorefrontProductsQuery> getQueryType() {
        return SearchStorefrontProductsQuery.class;
    }
}
