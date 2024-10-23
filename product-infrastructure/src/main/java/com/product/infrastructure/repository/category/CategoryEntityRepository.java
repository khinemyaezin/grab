package com.product.infrastructure.repository.category;

import com.nestedset.app.repository.NodeRepository;
import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryEntityRepository extends  EntityRepository<CategoryEntity, Long>, JpaRepository<CategoryEntity,Long>, NodeRepository<CategoryEntity,Long> {
}
