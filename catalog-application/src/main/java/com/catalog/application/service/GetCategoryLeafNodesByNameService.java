package com.catalog.application.service;

import com.catalog.application.port.inbound.GetCategoryLeafNodesByNameUseCase;

import com.catalog.application.port.outbound.CategoryQueryPort;
import com.catalog.application.model.read.CategoryView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.model.read.CategoryLeavesResult;
import com.catalog.application.model.read.CategoryResult;
import com.catalog.application.model.read.GetCategoryLeafNodesByNameQuery;

@lombok.RequiredArgsConstructor
public class GetCategoryLeafNodesByNameService implements GetCategoryLeafNodesByNameUseCase {

    private static final Logger log = Loggers.getLogger(GetCategoryLeafNodesByNameService.class);

    private final CategoryQueryPort categoryQueryRepository;

        public CategoryLeavesResult execute(GetCategoryLeafNodesByNameQuery query) {
        log.debug("Handling GetCategoryLeafNodesByNameQuery for name: {}", query.name());

        return new CategoryLeavesResult(
                categoryQueryRepository.findLeafNodesByName(query.name())
                        .stream()
                        .map(this::mapToResult)
                        .toList()
        );
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
