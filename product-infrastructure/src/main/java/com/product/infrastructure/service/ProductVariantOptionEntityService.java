package com.product.infrastructure.service;

import com.product.domain.entity.product_variant.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;

import java.util.List;
import java.util.Set;

public interface ProductVariantOptionEntityService {
    void updateVariations(ProductVariantEntity productVariantEntity, Set<ProductVariation> updatedVariantOptions);
}
