package com.product.infrastructure.service;

import com.product.domain.aggregate.product.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;

import java.util.Set;

public interface ProductVariantOptionService {
    void updateVariations(ProductVariantEntity productVariantEntity, Set<ProductVariation> updatedVariantOptions);
}
