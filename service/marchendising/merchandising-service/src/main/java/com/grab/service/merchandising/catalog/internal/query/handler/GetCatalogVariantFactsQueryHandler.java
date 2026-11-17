package com.grab.service.merchandising.catalog.internal.query.handler;

import com.catalog.application.model.read.GetCatalogVariantFactsQuery;
import com.catalog.application.model.read.GetCatalogProductFactsResult;
import com.catalog.application.port.inbound.GetCatalogVariantFactsUseCase;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.service.merchandising.catalog.internal.config.CatalogReadTransactional;
import org.springframework.stereotype.Component;

@Component
public class GetCatalogVariantFactsQueryHandler
        implements QueryHandler<GetCatalogVariantFactsQuery, GetCatalogProductFactsResult> {
    private final GetCatalogVariantFactsUseCase useCase;

    public GetCatalogVariantFactsQueryHandler(GetCatalogVariantFactsUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @CatalogReadTransactional
    public GetCatalogProductFactsResult handle(GetCatalogVariantFactsQuery query) {
        return useCase.execute(query).orElse(null);
    }

    @Override
    public Class<GetCatalogVariantFactsQuery> getQueryType() {
        return GetCatalogVariantFactsQuery.class;
    }
}
