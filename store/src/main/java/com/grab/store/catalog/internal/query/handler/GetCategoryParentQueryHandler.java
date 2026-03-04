package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRepo;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.query.CategoryResult;
import com.grab.store.catalog.internal.query.GetCategoryParentQuery;
import com.nestedset.app.NestedSetNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetCategoryParentQueryHandler implements QueryHandler<GetCategoryParentQuery, CategoryResult> {

    private final CategoryJpaRepo categoryJpaRepo;
    private final NestedSetNodeRepository<CategoryEntity, Long> nodeRepository;

    @Override
    public CategoryResult handle(GetCategoryParentQuery query) {
        log.debug("Handling GetCategoryParentQuery for categoryId: {}", query.categoryId());

        CategoryEntity category = categoryJpaRepo.findByUuid(query.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + query.categoryId()));

        CategoryEntity parent = nodeRepository.getParent(category)
                .orElseThrow(() -> new IllegalArgumentException("Parent category not found for category: " + query.categoryId()));

        return new CategoryResult(
                parent.getUuid(),
                parent.getName(),
                nodeRepository.getParent(parent).map(CategoryEntity::getUuid).orElse(null)
        );
    }

    @Override
    public Class<GetCategoryParentQuery> getQueryType() {
        return GetCategoryParentQuery.class;
    }
}
