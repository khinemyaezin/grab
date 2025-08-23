package com.product.infrastructure.repository.jpa;

import com.product.infrastructure.entity.category.entity.CategoryEntity;
import com.product.infrastructure.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryJpaRepository extends EntityRepository<CategoryEntity, Long>, JpaRepository<CategoryEntity,Long>{
    Optional<CategoryEntity> findByUuid(String uuid);
}
