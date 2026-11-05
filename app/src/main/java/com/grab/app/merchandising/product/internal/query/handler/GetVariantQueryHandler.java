package com.grab.app.merchandising.product.internal.query.handler;

import com.catalog.application.model.read.GetVariantQuery;
import com.catalog.application.model.read.GetVariantResult;
import com.catalog.application.port.inbound.GetVariantUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetVariantQueryHandler implements QueryHandler<GetVariantQuery, GetVariantResult> {

    private final GetVariantUseCase getVariantUseCase;

    public GetVariantQueryHandler(GetVariantUseCase getVariantUseCase) {
        this.getVariantUseCase = getVariantUseCase;
    }

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
