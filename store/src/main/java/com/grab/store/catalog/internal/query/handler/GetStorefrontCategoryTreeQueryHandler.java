package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.CategoryQueryRepository;
import com.catalog.infrastructure.view.CategoryNodeView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.queries.GetStorefrontCategoryTreeQuery;
import com.grab.store.catalog.queries.StorefrontCategoryNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetStorefrontCategoryTreeQueryHandler
        implements QueryHandler<GetStorefrontCategoryTreeQuery, List<StorefrontCategoryNode>> {

    private final CategoryQueryRepository categoryQueryRepository;

    @Override
    @CatalogReadTransactional
    public List<StorefrontCategoryNode> handle(GetStorefrontCategoryTreeQuery query) {
        if (!StringUtils.hasLength(query.rootId())) {
            return categoryQueryRepository.findRootTrees().stream()
                    .map(this::mapNode)
                    .toList();
        }
        return List.of(categoryQueryRepository.findTree(query.rootId())
                .map(this::mapNode)
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.CategoryNotFound(query.rootId())
                )));
    }

    @Override
    public Class<GetStorefrontCategoryTreeQuery> getQueryType() {
        return GetStorefrontCategoryTreeQuery.class;
    }

    private StorefrontCategoryNode mapNode(CategoryNodeView node) {
        return new StorefrontCategoryNode(
                node.id(),
                node.name(),
                node.parentId(),
                node.children().stream().map(this::mapNode).toList()
        );
    }
}
