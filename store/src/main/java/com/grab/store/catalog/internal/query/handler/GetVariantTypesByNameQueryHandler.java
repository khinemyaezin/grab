package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.GetVariantTypesByNameUseCase;
import com.catalog.application.query.GetVariantTypesByNameQuery;
import com.catalog.application.query.VariantTypeResult;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetVariantTypesByNameQueryHandler implements QueryHandler<GetVariantTypesByNameQuery, VariantTypeResult> {

    private final GetVariantTypesByNameUseCase getVariantTypesByNameUseCase;

    @Override
    @CatalogReadTransactional
    public VariantTypeResult handle(GetVariantTypesByNameQuery query) {
        return getVariantTypesByNameUseCase.execute(query);
    }

    @Override
    public Class<GetVariantTypesByNameQuery> getQueryType() {
        return GetVariantTypesByNameQuery.class;
    }
}
