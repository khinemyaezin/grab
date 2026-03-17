package com.grab.store.catalog.internal.query.handler;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.jpa.CategoryJpaRepo;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.CategoryResult;
import com.grab.store.catalog.internal.query.GetCategoryParentQuery;
import com.nestedset.app.NestedSetNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoryParentQueryHandler implements QueryHandler<GetCategoryParentQuery, CategoryResult> {

    private static final Logger log = Loggers.getLogger(GetCategoryParentQueryHandler.class);

    private final CategoryJpaRepo categoryJpaRepo;
    private final NestedSetNodeRepository<CategoryEntity, Long> nodeRepository;

    @Override
    @CatalogReadTransactional
    public CategoryResult handle(GetCategoryParentQuery query) {
        log.debug("Handling GetCategoryParentQuery for categoryId: {}", query.categoryId());

        CategoryEntity category = categoryJpaRepo.findByUuid(query.categoryId())
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.CategoryNotFound(query.categoryId())
                ));

        CategoryEntity parent = nodeRepository.getParent(category)
                .orElseThrow(() -> new CatalogServiceException(
                        new CatalogServiceError.ParentCategoryNotFoundForCategory(query.categoryId())
                ));

        return new CategoryResult(
                parent.getUuid(),
                parent.getName(),
                nodeRepository.getParent(parent).map(CategoryEntity::getUuid).orElse(null),
                Boolean.TRUE.equals(parent.getActive()),
                Boolean.TRUE.equals(parent.getListingAllowed()),
                Boolean.TRUE.equals(parent.getReviewRequired()),
                Boolean.TRUE.equals(parent.getC2cAllowed())
        );
    }

    @Override
    public Class<GetCategoryParentQuery> getQueryType() {
        return GetCategoryParentQuery.class;
    }
}
