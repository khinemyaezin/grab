package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.GetVariantUseCase;
import com.catalog.application.query.GetVariantQuery;
import com.catalog.application.query.GetVariantResult;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetVariantQueryHandler implements QueryHandler<GetVariantQuery, GetVariantResult> {

    private final GetVariantUseCase getVariantUseCase;

    @Override
    @CatalogReadTransactional
    public GetVariantResult handle(GetVariantQuery query) {
        return getVariantUseCase.execute(query);
    }

    @Override
    public Class<GetVariantQuery> getQueryType() {
        return GetVariantQuery.class;
    }
}
