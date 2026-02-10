package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.catalog.infrastructure.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductVariantJpaRepo extends EntityRepository<ProductVariantEntity, Long>, JpaRepository<ProductVariantEntity, Long>, JpaSpecificationExecutor<ProductVariantEntity> {
    Optional<ProductVariantEntity> findByUuid(String uuid);
}
