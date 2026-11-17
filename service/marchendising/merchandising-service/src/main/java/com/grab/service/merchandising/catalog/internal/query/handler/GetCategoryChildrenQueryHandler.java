package com.grab.service.merchandising.catalog.internal.query.handler;

import com.catalog.application.model.read.CategoryChildrenResult;
import com.catalog.application.model.read.GetCategoryChildrenQuery;
import com.catalog.application.port.inbound.GetCategoryChildrenUseCase;
import com.grab.service.merchandising.catalog.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetCategoryChildrenQueryHandler implements QueryHandler<GetCategoryChildrenQuery, CategoryChildrenResult> {

    private final GetCategoryChildrenUseCase getCategoryChildrenUseCase;

    public GetCategoryChildrenQueryHandler(GetCategoryChildrenUseCase getCategoryChildrenUseCase) {
        this.getCategoryChildrenUseCase = getCategoryChildrenUseCase;
    }

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
