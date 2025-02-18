package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;

import java.util.List;

public interface ProductVariantFactory {
    ProductVariant createVariant(Product parent, Id id, String sku, List<ProductVariation> variations);
}
