package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.GetCategoryChildrenUseCase;
import com.catalog.application.query.CategoryChildrenResult;
import com.catalog.application.query.GetCategoryChildrenQuery;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoryChildrenQueryHandler implements QueryHandler<GetCategoryChildrenQuery, CategoryChildrenResult> {

    private final GetCategoryChildrenUseCase getCategoryChildrenUseCase;

    @Override
    @CatalogReadTransactional
    public CategoryChildrenResult handle(GetCategoryChildrenQuery query) {
        return getCategoryChildrenUseCase.execute(query);
    }

    @Override
    public Class<GetCategoryChildrenQuery> getQueryType() {
        return GetCategoryChildrenQuery.class;
    }
}
