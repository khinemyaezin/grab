package com.product.infrastructure.repository.product_varaiant_option;

import com.product.infrastructure.entity.product.entity.ProductVariantOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantOptionRepository extends JpaRepository<ProductVariantOptionEntity,Long> {
}
