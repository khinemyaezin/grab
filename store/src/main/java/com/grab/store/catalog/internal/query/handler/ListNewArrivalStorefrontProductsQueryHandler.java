package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.queries.ListNewArrivalStorefrontProductsQuery;
import com.grab.store.catalog.queries.StorefrontProductSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListNewArrivalStorefrontProductsQueryHandler
        implements QueryHandler<ListNewArrivalStorefrontProductsQuery, Page<StorefrontProductSearchResult>> {

    private final StorefrontProductSearchMapper mapper;

    @Override
    @CatalogReadTransactional
    public Page<StorefrontProductSearchResult> handle(ListNewArrivalStorefrontProductsQuery query) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .storefrontVisible(true)
                .build();
        return mapper.search(criteria, withCreatedAtSort(query.pageable()));
    }

    @Override
    public Class<ListNewArrivalStorefrontProductsQuery> getQueryType() {
        return ListNewArrivalStorefrontProductsQuery.class;
    }

    private Pageable withCreatedAtSort(Pageable pageable) {
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }
}
