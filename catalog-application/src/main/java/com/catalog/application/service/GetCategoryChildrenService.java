package com.catalog.application.service;

import com.catalog.application.port.inbound.GetCategoryChildrenUseCase;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.readmodel.CategoryChildrenView;
import com.catalog.application.readmodel.CategoryView;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.query.CategoryChildrenResult;
import com.catalog.application.query.CategoryResult;
import com.catalog.application.query.GetCategoryChildrenQuery;
import lombok.RequiredArgsConstructor;

@lombok.RequiredArgsConstructor
public class GetCategoryChildrenService implements GetCategoryChildrenUseCase {

    private static final Logger log = Loggers.getLogger(GetCategoryChildrenService.class);

    private final CategoryQueryPort categoryQueryRepository;

        public CategoryChildrenResult execute(GetCategoryChildrenQuery query) {
        log.debug("Handling GetCategoryChildrenQuery for categoryId: {}", query.categoryId());

        return categoryQueryRepository.findChildren(query.categoryId())
                .map(this::mapChildrenView)
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.CategoryNotFound(query.categoryId())
                ));
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
                category.c2cAllowed()
        );
    }
}
