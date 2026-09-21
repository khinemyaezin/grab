package com.catalog.application.service;

import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.port.inbound.GetCategoryUseCase;
import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.model.read.CategoryResult;
import com.catalog.application.model.read.GetCategoryQuery;
import com.catalog.application.model.read.CategoryView;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.Logger;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetCategoryService implements GetCategoryUseCase {

    private static final Logger log = Loggers.getLogger(GetCategoryService.class);

    private final CategoryQueryPort categoryQueryRepository;

    public CategoryResult execute(GetCategoryQuery query) {
        log.debug("Handling GetCategoryQuery for categoryId: {}", query.categoryId());

        CategoryView category = categoryQueryRepository.findViewByIds(List.of(query.categoryId())).stream()
                .findFirst()
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.CategoryNotFound(query.categoryId())
                ));

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
