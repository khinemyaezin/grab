package com.grab.app.merchandising.product.internal.query.handler;

import com.catalog.application.model.read.GetVariantOptionsByNameQuery;
import com.catalog.application.model.read.VariantOptionResult;
import com.catalog.application.port.inbound.GetVariantOptionsByNameUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetVariantOptionsByNameQueryHandler implements QueryHandler<GetVariantOptionsByNameQuery, VariantOptionResult> {

    private final GetVariantOptionsByNameUseCase getVariantOptionsByNameUseCase;

    public GetVariantOptionsByNameQueryHandler(GetVariantOptionsByNameUseCase getVariantOptionsByNameUseCase) {
        this.getVariantOptionsByNameUseCase = getVariantOptionsByNameUseCase;
    }

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
