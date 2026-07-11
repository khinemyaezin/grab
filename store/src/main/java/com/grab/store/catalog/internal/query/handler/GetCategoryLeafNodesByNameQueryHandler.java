package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.view.CategoryView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.CategoryLeavesResult;
import com.grab.store.catalog.internal.query.CategoryResult;
import com.grab.store.catalog.internal.query.GetCategoryLeafNodesByNameQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoryLeafNodesByNameQueryHandler implements QueryHandler<GetCategoryLeafNodesByNameQuery, CategoryLeavesResult> {

    private static final Logger log = Loggers.getLogger(GetCategoryLeafNodesByNameQueryHandler.class);

    private final CategoryQueryRepository categoryQueryRepository;

    @Override
    @CatalogReadTransactional
    public CategoryLeavesResult handle(GetCategoryLeafNodesByNameQuery query) {
        log.debug("Handling GetCategoryLeafNodesByNameQuery for name: {}", query.name());

        return new CategoryLeavesResult(
                categoryQueryRepository.findLeafNodesByName(query.name())
                        .stream()
                        .map(this::mapToResult)
                        .toList()
        );
    }

    @Override
    public Class<GetCategoryLeafNodesByNameQuery> getQueryType() {
        return GetCategoryLeafNodesByNameQuery.class;
    }

    private CategoryResult mapToResult(CategoryView category) {
        return new CategoryResult(
                category.id(),
                category.name(),
                category.parentId(),
                category.active(),
                category.listingAllowed(),
                category.c2cAllowed()
        );
    }
}
