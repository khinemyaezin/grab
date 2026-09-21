package com.catalog.adapter.persistence.repository;

import com.catalog.adapter.persistence.entity.ProductVariantEntity;
import com.catalog.adapter.persistence.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductVariantJpaRepo extends EntityRepository<ProductVariantEntity, Long>, JpaRepository<ProductVariantEntity, Long>, JpaSpecificationExecutor<ProductVariantEntity> {
    Optional<ProductVariantEntity> findByUuid(String uuid);

    List<ProductVariantEntity> findByProduct_Uuid(String productUuid);
}
