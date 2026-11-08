package com.grab.app.merchandising.catalog.internal.query.handler;

import com.catalog.application.model.read.GetProductQuery;
import com.catalog.application.model.read.GetProductResult;
import com.catalog.application.port.inbound.GetProductUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetProductQueryHandler implements QueryHandler<GetProductQuery, GetProductResult> {

    private final GetProductUseCase getProductUseCase;

    public GetProductQueryHandler(GetProductUseCase getProductUseCase) {
        this.getProductUseCase = getProductUseCase;
    }

    @Override
    @CatalogReadTransactional
    public GetProductResult handle(GetProductQuery query) {
        return getProductUseCase.execute(query);
    }

    @Override
    public Class<GetProductQuery> getQueryType() {
        return GetProductQuery.class;
    }
}
