package com.grab.app.merchandising.catalog.internal.query.handler;

import com.catalog.application.model.read.CheckProductPublishableQuery;
import com.catalog.application.model.read.CheckProductPublishableResult;
import com.catalog.application.port.inbound.CheckProductPublishableUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class CheckProductPublishableQueryHandler implements QueryHandler<CheckProductPublishableQuery, CheckProductPublishableResult> {

    private final CheckProductPublishableUseCase checkProductPublishableUseCase;

    public CheckProductPublishableQueryHandler(CheckProductPublishableUseCase checkProductPublishableUseCase) {
        this.checkProductPublishableUseCase = checkProductPublishableUseCase;
    }

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
