package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.GetCategoryTreeUseCase;
import com.catalog.application.query.CategoryNodeResult;
import com.catalog.application.query.GetCategoryTreeQuery;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoryTreeQueryHandler implements QueryHandler<GetCategoryTreeQuery, CategoryNodeResult> {

    private final GetCategoryTreeUseCase getCategoryTreeUseCase;

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
