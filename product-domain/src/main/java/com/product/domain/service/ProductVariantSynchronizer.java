package com.product.domain.service;

import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.VariantType;

import java.util.List;

public interface ProductVariantSynchronizer {
    void synchronize(Product product, List<VariantType> desiredTypes);
}
