package com.grab.store.catalog.internal.query.handler;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.view.CategoryChildrenView;
import com.catalog.infrastructure.view.CategoryView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.CategoryChildrenResult;
import com.grab.store.catalog.internal.query.CategoryResult;
import com.grab.store.catalog.internal.query.GetCategoryChildrenQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoryChildrenQueryHandler implements QueryHandler<GetCategoryChildrenQuery, CategoryChildrenResult> {

    private static final Logger log = Loggers.getLogger(GetCategoryChildrenQueryHandler.class);

    private final CategoryQueryRepository categoryQueryRepository;

    @Override
    @CatalogReadTransactional
    public CategoryChildrenResult handle(GetCategoryChildrenQuery query) {
        log.debug("Handling GetCategoryChildrenQuery for categoryId: {}", query.categoryId());

        return categoryQueryRepository.findChildren(query.categoryId())
                .map(this::mapChildrenView)
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.CategoryNotFound(query.categoryId())
                ));
    }

    @Override
    public Class<GetCategoryChildrenQuery> getQueryType() {
        return GetCategoryChildrenQuery.class;
    }

    private CategoryChildrenResult mapChildrenView(CategoryChildrenView childrenView) {
        return new CategoryChildrenResult(
                childrenView.parentId(),
                childrenView.children().stream()
                        .map(this::mapCategory)
                        .toList()
        );
    }

    private CategoryResult mapCategory(CategoryView category) {
        return new CategoryResult(
                category.id(),
                category.name(),
                category.parentId(),
                category.active(),
                category.listingAllowed(),
                category.reviewRequired(),
                category.c2cAllowed()
        );
    }
}
