package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.GetProductBySlugUseCase;
import com.catalog.application.query.GetProductBySlugQuery;
import com.catalog.application.query.GetProductBySlugResult;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProductBySlugQueryHandler implements QueryHandler<GetProductBySlugQuery, GetProductBySlugResult> {

    private final GetProductBySlugUseCase getProductBySlugUseCase;

    @Override
    @CatalogReadTransactional
    public GetProductBySlugResult handle(GetProductBySlugQuery query) {
        return getProductBySlugUseCase.execute(query);
    }

    @Override
    public Class<GetProductBySlugQuery> getQueryType() {
        return GetProductBySlugQuery.class;
    }
}
