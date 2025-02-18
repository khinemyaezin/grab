package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;

import java.util.List;

public class ProductVariantFactoryImpl implements ProductVariantFactory{

    @Override
    public ProductVariant createVariant(Product parent, Id id, String sku, List<ProductVariation> variations) {
        return new ProductVariant(
                id,
                parent.getId().orElseThrow(),
                sku,
                variations
        );
    }
}
