package com.product.domain.factory.product;

import com.product.domain.entity.product.Product;

public interface ProductFactory {
    Product createProduct(String productId, String name, String categoryId);
}
