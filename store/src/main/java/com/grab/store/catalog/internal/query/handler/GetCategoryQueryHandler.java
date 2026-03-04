package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.repository.CategoryRepository;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.CategoryResult;
import com.grab.store.catalog.internal.query.GetCategoryQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetCategoryQueryHandler implements QueryHandler<GetCategoryQuery, CategoryResult> {

    private final CategoryRepository categoryRepository;
    private final IdGenerator idGenerator;

    @Override
    @CatalogReadTransactional
    public CategoryResult handle(GetCategoryQuery query) {
        log.debug("Handling GetCategoryQuery for categoryId: {}", query.categoryId());

        Category category = categoryRepository.find(idGenerator.generateId(query.categoryId()))
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + query.categoryId()));

        return new CategoryResult(
                category.getId().getValue(),
                category.getName(),
                category.getParentId().map(id -> id.getValue()).orElse(null)
        );
    }

    @Override
    public Class<GetCategoryQuery> getQueryType() {
        return GetCategoryQuery.class;
    }
}
