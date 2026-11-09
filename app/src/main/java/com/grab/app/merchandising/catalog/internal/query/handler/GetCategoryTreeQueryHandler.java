package com.grab.app.merchandising.catalog.internal.query.handler;

import com.catalog.application.model.read.CategoryNodeResult;
import com.catalog.application.model.read.GetCategoryTreeQuery;
import com.catalog.application.port.inbound.GetCategoryTreeUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetCategoryTreeQueryHandler implements QueryHandler<GetCategoryTreeQuery, CategoryNodeResult> {

    private final GetCategoryTreeUseCase getCategoryTreeUseCase;

    public GetCategoryTreeQueryHandler(GetCategoryTreeUseCase getCategoryTreeUseCase) {
        this.getCategoryTreeUseCase = getCategoryTreeUseCase;
    }

    @Override
    @CatalogReadTransactional
    public CategoryNodeResult handle(GetCategoryTreeQuery query) {
        return getCategoryTreeUseCase.execute(query);
    }

    @Override
    public Class<GetCategoryTreeQuery> getQueryType() {
        return GetCategoryTreeQuery.class;
    }
}
