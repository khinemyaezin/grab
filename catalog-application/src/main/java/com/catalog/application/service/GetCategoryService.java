package com.catalog.application.service;

import com.catalog.application.port.inbound.GetCategoryUseCase;

import com.grab.framework.id.Id;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.port.outbound.CategoryRepository;
import com.grab.framework.id.IdGenerator;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.query.CategoryResult;
import com.catalog.application.query.GetCategoryQuery;
import lombok.RequiredArgsConstructor;

@lombok.RequiredArgsConstructor
public class GetCategoryService implements GetCategoryUseCase {

    private static final Logger log = Loggers.getLogger(GetCategoryService.class);

    private final CategoryRepository categoryRepository;
    private final IdGenerator idGenerator;

        public CategoryResult execute(GetCategoryQuery query) {
        log.debug("Handling GetCategoryQuery for categoryId: {}", query.categoryId());

        Category category = categoryRepository.find(idGenerator.convertIdFrom(query.categoryId()))
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.CategoryNotFound(query.categoryId())
                ));

        return new CategoryResult(
                category.getId().getValue(),
                category.getName(),
                category.getParentId().map(Id::getValue).orElse(null),
                category.isActive(),
                category.isListingAllowed(),
                category.isC2cAllowed()
        );
    }
}
