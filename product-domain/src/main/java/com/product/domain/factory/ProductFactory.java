package com.product.domain.factory;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductSpec;

public interface ProductFactory {
    Product create(String name, Id categoryId);
}
