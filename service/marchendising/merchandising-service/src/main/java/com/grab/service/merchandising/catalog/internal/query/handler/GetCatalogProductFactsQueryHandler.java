package com.grab.service.merchandising.catalog.internal.query.handler;

import com.catalog.application.model.read.GetCatalogProductFactsQuery;
import com.catalog.application.model.read.GetCatalogProductFactsResult;
import com.catalog.application.port.inbound.GetCatalogProductFactsUseCase;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.service.merchandising.catalog.internal.config.CatalogReadTransactional;
import org.springframework.stereotype.Component;

@Component
public class GetCatalogProductFactsQueryHandler
        implements QueryHandler<GetCatalogProductFactsQuery, GetCatalogProductFactsResult> {
    private final GetCatalogProductFactsUseCase useCase;

    public GetCatalogProductFactsQueryHandler(GetCatalogProductFactsUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    @CatalogReadTransactional
    public GetCatalogProductFactsResult handle(GetCatalogProductFactsQuery query) {
        return useCase.execute(query).orElse(null);
    }

    @Override
    public Class<GetCatalogProductFactsQuery> getQueryType() {
        return GetCatalogProductFactsQuery.class;
    }
}
