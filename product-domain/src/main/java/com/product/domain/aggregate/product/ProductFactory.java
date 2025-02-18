package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;

public interface ProductFactory {
    Product createProduct(Id id, String name, Id categoryId);
}
