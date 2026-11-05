package com.grab.app.merchandising.product.internal.query.handler;

import com.catalog.application.model.read.GetProductBySlugQuery;
import com.catalog.application.model.read.GetProductBySlugResult;
import com.catalog.application.port.inbound.GetProductBySlugUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetProductBySlugQueryHandler implements QueryHandler<GetProductBySlugQuery, GetProductBySlugResult> {

    private final GetProductBySlugUseCase getProductBySlugUseCase;

    public GetProductBySlugQueryHandler(GetProductBySlugUseCase getProductBySlugUseCase) {
        this.getProductBySlugUseCase = getProductBySlugUseCase;
    }

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
