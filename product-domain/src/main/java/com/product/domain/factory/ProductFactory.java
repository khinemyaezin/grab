package com.product.domain.factory;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductSpec;
import com.product.domain.aggregate.product.VariantType;

import java.util.List;

public interface ProductFactory {
    void create(Product product, List<VariantType> desiredVariantTypes, List<Id> variantIdsToRemove);
    Product create(ProductSpec spec);
}
