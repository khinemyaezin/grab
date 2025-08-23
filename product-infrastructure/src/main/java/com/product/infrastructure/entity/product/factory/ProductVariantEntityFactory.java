package com.product.infrastructure.entity.product.factory;

import com.product.domain.aggregate.product.ProductVariant;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.mapper.product.ProductVariantEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class ProductVariantEntityFactory {
    private final ProductVariantEntityMapper productVariantEntityMapper;

    public ProductVariantEntityFactory(ProductVariantEntityMapper productVariantEntityMapper) {
        this.productVariantEntityMapper = productVariantEntityMapper;
    }

    public ProductVariantEntity create(ProductVariant productVariant) {
        ProductVariantEntity newVariant = new ProductVariantEntity();
        productVariantEntityMapper.map(productVariant, newVariant);
        return newVariant;
    }
}
