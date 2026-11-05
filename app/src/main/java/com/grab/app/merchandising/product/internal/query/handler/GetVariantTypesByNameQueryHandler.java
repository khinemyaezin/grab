package com.grab.app.merchandising.product.internal.query.handler;

import com.catalog.application.model.read.GetVariantTypesByNameQuery;
import com.catalog.application.model.read.VariantTypeResult;
import com.catalog.application.port.inbound.GetVariantTypesByNameUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetVariantTypesByNameQueryHandler implements QueryHandler<GetVariantTypesByNameQuery, VariantTypeResult> {

    private final GetVariantTypesByNameUseCase getVariantTypesByNameUseCase;

    public GetVariantTypesByNameQueryHandler(GetVariantTypesByNameUseCase getVariantTypesByNameUseCase) {
        this.getVariantTypesByNameUseCase = getVariantTypesByNameUseCase;
    }

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
