package com.product.domain.entity.product;

import com.product.domain.entity.variant_type.VariantType;

import java.util.List;

public class ProductFactoryImpl {

    public Product createProduct(String id, String name, String categoryId) {
       return new Product(id,name,categoryId);
    }


}
