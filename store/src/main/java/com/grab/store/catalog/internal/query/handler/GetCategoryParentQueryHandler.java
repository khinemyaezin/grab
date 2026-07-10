package com.grab.store.catalog.internal.query.handler;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.view.CategoryView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.CategoryResult;
import com.grab.store.catalog.internal.query.GetCategoryParentQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoryParentQueryHandler implements QueryHandler<GetCategoryParentQuery, CategoryResult> {

    private static final Logger log = Loggers.getLogger(GetCategoryParentQueryHandler.class);

    private final CategoryQueryRepository categoryQueryRepository;

    @Override
    @CatalogReadTransactional
    public CategoryResult handle(GetCategoryParentQuery query) {
        log.debug("Handling GetCategoryParentQuery for categoryId: {}", query.categoryId());

        if (!categoryQueryRepository.exists(query.categoryId())) {
            throw new CatalogServiceException(
                    new CatalogServiceError.CategoryNotFound(query.categoryId())
            );
        }

        return categoryQueryRepository.findParent(query.categoryId())
                .map(this::mapCategory)
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ParentCategoryNotFoundForCategory(query.categoryId())
                ));
    }

    @Override
    public Class<GetCategoryParentQuery> getQueryType() {
        return GetCategoryParentQuery.class;
    }

    private CategoryResult mapCategory(CategoryView category) {
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
