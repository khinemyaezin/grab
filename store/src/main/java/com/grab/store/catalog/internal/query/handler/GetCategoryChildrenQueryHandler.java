package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRepo;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.query.CategoryChildrenResult;
import com.grab.store.catalog.internal.query.CategoryResult;
import com.grab.store.catalog.internal.query.GetCategoryChildrenQuery;
import com.nestedset.app.NestedSetNodeRepository;
import com.nestedset.app.service.TreeBuilder;
import com.nestedset.library.model.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetCategoryChildrenQueryHandler implements QueryHandler<GetCategoryChildrenQuery, CategoryChildrenResult> {

    private final CategoryJpaRepo categoryJpaRepo;
    private final NestedSetNodeRepository<CategoryEntity, Long> nodeRepository;

    @Override
    public CategoryChildrenResult handle(GetCategoryChildrenQuery query) {
        log.debug("Handling GetCategoryChildrenQuery for categoryId: {}", query.categoryId());

        CategoryEntity category = categoryJpaRepo.findByUuid(query.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + query.categoryId()));

        NodeComponent<CategoryEntity> tree = nodeRepository.getImmediateChildren(category);
        List<CategoryResult> children = mapChildren(safeChildren(tree), category.getUuid());

        return new CategoryChildrenResult(category.getUuid(), children);
    }

    @Override
    public Class<GetCategoryChildrenQuery> getQueryType() {
        return GetCategoryChildrenQuery.class;
    }

    private List<CategoryResult> mapChildren(Set<NodeComponent<CategoryEntity>> children, String parentId) {
        if (children == null || children.isEmpty()) {
            return List.of();
        }
        return children.stream()
                .map(child -> mapCategory(child.getNode(), parentId))
                .toList();
    }

    private Set<NodeComponent<CategoryEntity>> safeChildren(NodeComponent<CategoryEntity> node) {
        try {
            return node.getChildren();
        } catch (UnsupportedOperationException ex) {
            return Set.of();
        }
    }

    private CategoryResult mapCategory(CategoryEntity entity, String parentId) {
        if (entity == null) {
            return null;
        }
        return new CategoryResult(
                entity.getUuid(),
                entity.getName(),
                parentId
        );
    }
}
