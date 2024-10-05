package com.coolstuff.ecommerce.grab.infrastructure.product.repository.category;

import com.coolstuff.core.nestedset.repository.JpaNodeRepository;
import com.coolstuff.ecommerce.grab.infrastructure.product.entity.category.CategoryEntity;
import com.coolstuff.ecommerce.grab.infrastructure.product.repository.EntityRepository;

public interface CategoryEntityRepository extends EntityRepository<CategoryEntity,Long>, JpaNodeRepository<CategoryEntity, Long> {
}
