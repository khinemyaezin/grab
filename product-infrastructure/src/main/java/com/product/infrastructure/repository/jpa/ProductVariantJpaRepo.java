package com.product.infrastructure.repository.jpa;

import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductVariantJpaRepo extends EntityRepository<ProductVariantEntity, Long>, JpaRepository<ProductVariantEntity, Long>, JpaSpecificationExecutor<ProductVariantEntity> {
    Optional<ProductVariantEntity> findByUuid(String uuid);
}
