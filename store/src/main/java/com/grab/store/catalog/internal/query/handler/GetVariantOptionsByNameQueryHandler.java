package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.GetVariantOptionsByNameUseCase;
import com.catalog.application.query.GetVariantOptionsByNameQuery;
import com.catalog.application.query.VariantOptionResult;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetVariantOptionsByNameQueryHandler implements QueryHandler<GetVariantOptionsByNameQuery, VariantOptionResult> {

    private final GetVariantOptionsByNameUseCase getVariantOptionsByNameUseCase;

    @Override
    @CatalogReadTransactional
    public VariantOptionResult handle(GetVariantOptionsByNameQuery query) {
        return getVariantOptionsByNameUseCase.execute(query);
    }

    @Override
    public Class<GetVariantOptionsByNameQuery> getQueryType() {
        return GetVariantOptionsByNameQuery.class;
    }
}
