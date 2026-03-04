package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRepo;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.CategoryNodeResult;
import com.grab.store.catalog.internal.query.GetCategoryTreeQuery;
import com.nestedset.app.NestedSetNodeRepository;
import com.nestedset.library.model.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetCategoryTreeQueryHandler implements QueryHandler<GetCategoryTreeQuery, CategoryNodeResult> {

    private final CategoryJpaRepo categoryJpaRepo;
    private final NestedSetNodeRepository<CategoryEntity, Long> nodeRepository;

    @Override
    @CatalogReadTransactional
    public CategoryNodeResult handle(GetCategoryTreeQuery query) {
        log.debug("Handling GetCategoryTreeQuery for categoryId: {}", query.categoryId());

        CategoryEntity category = categoryJpaRepo.findByUuid(query.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + query.categoryId()));

        NodeComponent<CategoryEntity> tree = nodeRepository.getTree(category);
        return mapNode(tree);
    }

    @Override
    public Class<GetCategoryTreeQuery> getQueryType() {
        return GetCategoryTreeQuery.class;
    }

    private CategoryNodeResult mapNode(NodeComponent<CategoryEntity> node) {
        if (node == null || node.getNode() == null) {
            return null;
        }
        CategoryEntity entity = node.getNode();
        String parentId = node.getParent() != null && node.getParent().getNode() != null
                ? node.getParent().getNode().getUuid()
                : null;

        List<CategoryNodeResult> children = mapChildren(safeChildren(node));

        return new CategoryNodeResult(
                entity.getUuid(),
                entity.getName(),
                parentId,
                children
        );
    }

    private List<CategoryNodeResult> mapChildren(Set<NodeComponent<CategoryEntity>> children) {
        if (children == null || children.isEmpty()) {
            return List.of();
        }
        return children.stream()
                .map(this::mapNode)
                .toList();
    }

    private Set<NodeComponent<CategoryEntity>> safeChildren(NodeComponent<CategoryEntity> node) {
        try {
            return node.getChildren();
        } catch (UnsupportedOperationException ex) {
            return Set.of();
        }
    }
}
