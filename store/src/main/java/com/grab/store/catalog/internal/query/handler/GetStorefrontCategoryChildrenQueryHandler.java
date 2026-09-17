package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.view.CategoryChildrenView;
import com.catalog.infrastructure.view.CategoryView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.queries.GetStorefrontCategoryChildrenQuery;
import com.grab.store.catalog.queries.StorefrontCategoryChild;
import com.grab.store.catalog.queries.StorefrontCategoryChildren;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetStorefrontCategoryChildrenQueryHandler
        implements QueryHandler<GetStorefrontCategoryChildrenQuery, StorefrontCategoryChildren> {

    private final CategoryQueryRepository categoryQueryRepository;

    @Override
    @CatalogReadTransactional
    public StorefrontCategoryChildren handle(GetStorefrontCategoryChildrenQuery query) {
        return categoryQueryRepository.findChildren(query.categoryId())
                .map(this::mapChildren)
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.CategoryNotFound(query.categoryId())
                ));
    }

    @Override
    public Class<GetStorefrontCategoryChildrenQuery> getQueryType() {
        return GetStorefrontCategoryChildrenQuery.class;
    }

    private StorefrontCategoryChildren mapChildren(CategoryChildrenView view) {
        return new StorefrontCategoryChildren(
                view.parentId(),
                view.children().stream().map(this::mapChild).toList()
        );
    }

    private StorefrontCategoryChild mapChild(CategoryView category) {
        return new StorefrontCategoryChild(
                category.id(),
                category.name(),
                category.parentId(),
                category.active(),
                category.listingAllowed(),
                category.c2cAllowed()
        );
    }
}
