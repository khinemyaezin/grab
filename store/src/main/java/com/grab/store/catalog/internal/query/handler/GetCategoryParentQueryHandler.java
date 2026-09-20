package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.GetCategoryParentUseCase;
import com.catalog.application.query.CategoryResult;
import com.catalog.application.query.GetCategoryParentQuery;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoryParentQueryHandler implements QueryHandler<GetCategoryParentQuery, CategoryResult> {

    private final GetCategoryParentUseCase getCategoryParentUseCase;

    @Override
    @CatalogReadTransactional
    public CategoryResult handle(GetCategoryParentQuery query) {
        return getCategoryParentUseCase.execute(query);
    }

    @Override
    public Class<GetCategoryParentQuery> getQueryType() {
        return GetCategoryParentQuery.class;
    }
}
