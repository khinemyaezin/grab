package com.grab.app.merchandising.product.internal.query.handler;

import com.catalog.application.model.read.CategoryResult;
import com.catalog.application.model.read.GetCategoryParentQuery;
import com.catalog.application.port.inbound.GetCategoryParentUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetCategoryParentQueryHandler implements QueryHandler<GetCategoryParentQuery, CategoryResult> {

    private final GetCategoryParentUseCase getCategoryParentUseCase;

    public GetCategoryParentQueryHandler(GetCategoryParentUseCase getCategoryParentUseCase) {
        this.getCategoryParentUseCase = getCategoryParentUseCase;
    }

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
