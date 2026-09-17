package com.grab.store.catalog.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.GetProductBySlugQuery;
import com.grab.store.catalog.queries.GetProductBySlugResult;
import com.grab.store.catalog.queries.GetStorefrontProductBySlugQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetStorefrontProductBySlugQueryHandler
        implements QueryHandler<GetStorefrontProductBySlugQuery, GetProductBySlugResult> {

    private final GetProductBySlugQueryHandler getProductBySlugQueryHandler;

    @Override
    @CatalogReadTransactional
    public GetProductBySlugResult handle(GetStorefrontProductBySlugQuery query) {
        return getProductBySlugQueryHandler.handle(new GetProductBySlugQuery(query.slug()));
    }

    @Override
    public Class<GetStorefrontProductBySlugQuery> getQueryType() {
        return GetStorefrontProductBySlugQuery.class;
    }
}
