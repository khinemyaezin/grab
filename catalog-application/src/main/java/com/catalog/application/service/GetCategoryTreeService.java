package com.catalog.application.service;

import com.catalog.application.port.inbound.GetCategoryTreeUseCase;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.readmodel.CategoryNodeView;
import com.catalog.application.exception.CatalogServiceError;
import com.catalog.application.exception.CatalogServiceException;
import com.catalog.application.query.CategoryNodeResult;
import com.catalog.application.query.GetCategoryTreeQuery;
import lombok.RequiredArgsConstructor;

@lombok.RequiredArgsConstructor
public class GetCategoryTreeService implements GetCategoryTreeUseCase {

    private static final Logger log = Loggers.getLogger(GetCategoryTreeService.class);

    private final CategoryQueryPort categoryQueryRepository;

        public CategoryNodeResult execute(GetCategoryTreeQuery query) {
        log.debug("Handling GetCategoryTreeQuery for categoryId: {}", query.categoryId());

        return categoryQueryRepository.findTree(query.categoryId())
                .map(this::mapNode)
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.CategoryNotFound(query.categoryId())
                ));
    }

    private CategoryNodeResult mapNode(CategoryNodeView node) {
        return new CategoryNodeResult(
                node.id(),
                node.name(),
                node.parentId(),
                node.children().stream()
                        .map(this::mapNode)
                        .toList()
        );
    }
}
