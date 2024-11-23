package com.product.infrastructure.repository.product_variant;

import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductVariantEntityRepository extends JpaRepository<ProductVariantEntity,Long> {
    Optional<ProductVariantEntity> findByUuid(String uuid);
}
