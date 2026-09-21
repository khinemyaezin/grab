package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.GetProductUseCase;
import com.catalog.application.model.read.GetProductQuery;
import com.catalog.application.model.read.GetProductResult;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProductQueryHandler implements QueryHandler<GetProductQuery, GetProductResult> {

    private final GetProductUseCase getProductUseCase;

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
