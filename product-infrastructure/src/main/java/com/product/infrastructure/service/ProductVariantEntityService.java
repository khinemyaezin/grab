package com.product.infrastructure.service;


import com.product.domain.entity.product_variant.ProductVariant;
import com.product.infrastructure.entity.product.entity.ProductEntity;

import java.util.List;

public interface ProductVariantEntityService {
    void updateVariants(ProductEntity productEntity, List<ProductVariant> productVariants);
}
