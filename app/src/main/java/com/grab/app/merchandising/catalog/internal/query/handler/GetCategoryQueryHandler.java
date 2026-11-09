package com.grab.app.merchandising.catalog.internal.query.handler;

import com.catalog.application.model.read.CategoryResult;
import com.catalog.application.model.read.GetCategoryQuery;
import com.catalog.application.port.inbound.GetCategoryUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetCategoryQueryHandler implements QueryHandler<GetCategoryQuery, CategoryResult> {

    private final GetCategoryUseCase getCategoryUseCase;

    public GetCategoryQueryHandler(GetCategoryUseCase getCategoryUseCase) {
        this.getCategoryUseCase = getCategoryUseCase;
    }

    @Override
    @CatalogReadTransactional
    public CategoryResult handle(GetCategoryQuery query) {
        return getCategoryUseCase.execute(query);
    }

    @Override
    public Class<GetCategoryQuery> getQueryType() {
        return GetCategoryQuery.class;
    }
}
