package com.grab.store.catalog.internal.query.handler;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.domain.aggregate.Category;
import com.catalog.domain.repository.CategoryRepository;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.CategoryResult;
import com.grab.store.catalog.internal.query.GetCategoryQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoryQueryHandler implements QueryHandler<GetCategoryQuery, CategoryResult> {

    private static final Logger log = Loggers.getLogger(GetCategoryQueryHandler.class);

    private final CategoryRepository categoryRepository;
    private final IdGenerator idGenerator;

    @Override
    @CatalogReadTransactional
    public CategoryResult handle(GetCategoryQuery query) {
        log.debug("Handling GetCategoryQuery for categoryId: {}", query.categoryId());

        Category category = categoryRepository.find(idGenerator.convertIdFrom(query.categoryId()))
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.CategoryNotFound(query.categoryId())
                ));

        return new CategoryResult(
                category.getId().getValue(),
                category.getName(),
                category.getParentId().map(id -> id.getValue()).orElse(null),
                category.isActive(),
                category.isListingAllowed(),
                category.isReviewRequired(),
                category.isC2cAllowed()
        );
    }

    @Override
    public Class<GetCategoryQuery> getQueryType() {
        return GetCategoryQuery.class;
    }
}
