package com.category.infrastructure.repository.jpa;

import com.category.infrastructure.entity.CategoryEntity;
import com.category.infrastructure.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryJpaRepository extends EntityRepository<CategoryEntity, Long>, JpaRepository<CategoryEntity,Long>{
    Optional<CategoryEntity> findByUuid(String uuid);
}
