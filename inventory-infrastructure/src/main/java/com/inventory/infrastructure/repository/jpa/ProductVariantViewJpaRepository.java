package com.inventory.infrastructure.repository.jpa;

import com.inventory.infrastructure.entity.ProductVariantViewEntity;
import com.inventory.infrastructure.view.ProductView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantViewJpaRepository extends JpaRepository<ProductVariantViewEntity, Long> {
    Optional<ProductVariantViewEntity> findByVariantUuid(String variantUuid);

    List<ProductVariantViewEntity> findAllByProductUuid(String productUuid);

    Optional<ProductView> findBySkuAndStatus(String sku, String status);

    List<ProductView> findAllBySkuIn(Collection<String> skus);

    List<ProductView> findBySkuContainingIgnoreCaseAndStatus(String sku, String status);
}
