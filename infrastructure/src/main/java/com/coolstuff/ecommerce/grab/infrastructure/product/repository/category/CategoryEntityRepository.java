package com.coolstuff.ecommerce.grab.infrastructure.product.repository.category;

import com.coolstuff.ecommerce.grab.infrastructure.product.entity.category.CategoryEntity;
import com.coolstuff.ecommerce.grab.infrastructure.product.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryEntityRepository extends EntityRepository<CategoryEntity>,JpaRepository<CategoryEntity,Long> {
}
