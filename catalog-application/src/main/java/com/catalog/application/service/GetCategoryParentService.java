package com.catalog.application.service;

import com.catalog.application.port.inbound.GetCategoryParentUseCase;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.readmodel.CategoryView;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.query.CategoryResult;
import com.catalog.application.query.GetCategoryParentQuery;
import lombok.RequiredArgsConstructor;

@lombok.RequiredArgsConstructor
public class GetCategoryParentService implements GetCategoryParentUseCase {

    private static final Logger log = Loggers.getLogger(GetCategoryParentService.class);

    private final CategoryQueryPort categoryQueryRepository;

        public CategoryResult execute(GetCategoryParentQuery query) {
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
