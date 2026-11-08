package com.grab.app.merchandising.catalog.internal.query.handler;

import com.catalog.application.model.read.GetProductAuditQuery;
import com.catalog.application.model.read.GetProductAuditResult;
import com.catalog.application.port.inbound.GetProductAuditUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetProductAuditQueryHandler implements QueryHandler<GetProductAuditQuery, GetProductAuditResult> {

    private final GetProductAuditUseCase getProductAuditUseCase;

    public GetProductAuditQueryHandler(GetProductAuditUseCase getProductAuditUseCase) {
        this.getProductAuditUseCase = getProductAuditUseCase;
    }

    @Override
    @CatalogReadTransactional
    public GetProductAuditResult handle(GetProductAuditQuery query) {
        return getProductAuditUseCase.execute(query);
    }

    @Override
    public Class<GetProductAuditQuery> getQueryType() {
        return GetProductAuditQuery.class;
    }
}
