package com.grab.store.catalog.internal.query.handler;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.view.CategoryNodeView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.CategoryNodeResult;
import com.grab.store.catalog.internal.query.GetCategoryTreeQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoryTreeQueryHandler implements QueryHandler<GetCategoryTreeQuery, CategoryNodeResult> {

    private static final Logger log = Loggers.getLogger(GetCategoryTreeQueryHandler.class);

    private final CategoryQueryRepository categoryQueryRepository;

    @Override
    @CatalogReadTransactional
    public CategoryNodeResult handle(GetCategoryTreeQuery query) {
        log.debug("Handling GetCategoryTreeQuery for categoryId: {}", query.categoryId());

        return categoryQueryRepository.findTree(query.categoryId())
                .map(this::mapNode)
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.CategoryNotFound(query.categoryId())
                ));
    }

    @Override
    public Class<GetCategoryTreeQuery> getQueryType() {
        return GetCategoryTreeQuery.class;
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
