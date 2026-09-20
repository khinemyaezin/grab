package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.GetCategoryUseCase;
import com.catalog.application.model.read.CategoryResult;
import com.catalog.application.model.read.GetCategoryQuery;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoryQueryHandler implements QueryHandler<GetCategoryQuery, CategoryResult> {

    private final GetCategoryUseCase getCategoryUseCase;

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
