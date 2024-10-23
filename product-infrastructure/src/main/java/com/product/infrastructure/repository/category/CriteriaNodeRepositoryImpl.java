package com.product.infrastructure.repository.category;

import com.nestedset.app.repository.AbstractCriteriaNodeRepository;
import com.product.infrastructure.entity.category.CategoryEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class CriteriaNodeRepositoryImpl extends AbstractCriteriaNodeRepository<CategoryEntity, Long> {

    protected CriteriaNodeRepositoryImpl(EntityManager entityManager, Class<CategoryEntity> entityClassType) {
        super(entityManager, entityClassType);
    }
}
