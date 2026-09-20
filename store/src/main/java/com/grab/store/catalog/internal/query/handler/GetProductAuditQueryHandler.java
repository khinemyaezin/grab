package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.GetProductAuditUseCase;
import com.catalog.application.query.GetProductAuditQuery;
import com.catalog.application.query.GetProductAuditResult;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProductAuditQueryHandler implements QueryHandler<GetProductAuditQuery, GetProductAuditResult> {

    private final GetProductAuditUseCase getProductAuditUseCase;

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
