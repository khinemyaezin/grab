package com.product.domain.aggregate.product;

import com.grab.framework.id.Id;

public class ProductFactoryImpl implements ProductFactory{
    public Product createProduct(Id id, String name, Id categoryId) {
       return new Product(id,name,categoryId);
    }
}
