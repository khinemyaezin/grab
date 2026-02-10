package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.CategoryEntity;
import com.catalog.infrastructure.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryJpaRepo extends EntityRepository<CategoryEntity, Long>, JpaRepository<CategoryEntity,Long>{
    Optional<CategoryEntity> findByUuid(String uuid);
}
