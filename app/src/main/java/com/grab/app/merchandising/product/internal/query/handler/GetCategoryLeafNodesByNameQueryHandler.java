package com.grab.app.merchandising.product.internal.query.handler;

import com.catalog.application.model.read.CategoryLeavesResult;
import com.catalog.application.model.read.GetCategoryLeafNodesByNameQuery;
import com.catalog.application.port.inbound.GetCategoryLeafNodesByNameUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetCategoryLeafNodesByNameQueryHandler implements QueryHandler<GetCategoryLeafNodesByNameQuery, CategoryLeavesResult> {

    private final GetCategoryLeafNodesByNameUseCase getCategoryLeafNodesByNameUseCase;

    public GetCategoryLeafNodesByNameQueryHandler(GetCategoryLeafNodesByNameUseCase getCategoryLeafNodesByNameUseCase) {
        this.getCategoryLeafNodesByNameUseCase = getCategoryLeafNodesByNameUseCase;
    }

    @Override
    @CatalogReadTransactional
    public CategoryLeavesResult handle(GetCategoryLeafNodesByNameQuery query) {
        return getCategoryLeafNodesByNameUseCase.execute(query);
    }

    @Override
    public Class<GetCategoryLeafNodesByNameQuery> getQueryType() {
        return GetCategoryLeafNodesByNameQuery.class;
    }
}
