package com.product.infrastructure.service;


import com.product.domain.aggregate.product.ProductVariant;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;

import java.util.List;
import java.util.Optional;

public interface ProductVariantService {
    void updateVariants(ProductEntity productEntity, List<ProductVariant> productVariants);
    Optional<ProductVariantEntity> find(String uuid);
}
