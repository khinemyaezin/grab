package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.CheckProductPublishableUseCase;
import com.catalog.application.model.read.CheckProductPublishableQuery;
import com.catalog.application.model.read.CheckProductPublishableResult;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckProductPublishableQueryHandler implements QueryHandler<CheckProductPublishableQuery, CheckProductPublishableResult> {

    private final CheckProductPublishableUseCase checkProductPublishableUseCase;

    @Override
    @CatalogReadTransactional
    public CheckProductPublishableResult handle(CheckProductPublishableQuery query) {
        return checkProductPublishableUseCase.execute(query);
    }

    @Override
    public Class<CheckProductPublishableQuery> getQueryType() {
        return CheckProductPublishableQuery.class;
    }
}
