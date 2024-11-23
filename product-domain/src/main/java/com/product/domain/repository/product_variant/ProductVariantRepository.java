package com.product.domain.repository.product_variant;

import com.product.domain.entity.product_variant.ProductVariant;

public interface ProductVariantRepository {
    ProductVariant save(ProductVariant productVariant);
    void delete(String id);
}
