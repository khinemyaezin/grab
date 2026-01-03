package com.product.domain.factory;

import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductSpec;

public interface ProductFactory {
    Product create(ProductSpec spec);
}
