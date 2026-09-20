package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.GetCategoryLeafNodesByNameUseCase;
import com.catalog.application.model.read.CategoryLeavesResult;
import com.catalog.application.model.read.GetCategoryLeafNodesByNameQuery;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoryLeafNodesByNameQueryHandler implements QueryHandler<GetCategoryLeafNodesByNameQuery, CategoryLeavesResult> {

    private final GetCategoryLeafNodesByNameUseCase getCategoryLeafNodesByNameUseCase;

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
