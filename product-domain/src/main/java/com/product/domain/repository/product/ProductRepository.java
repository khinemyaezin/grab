package com.product.domain.repository.product;

import com.product.domain.entity.product.Product;

public interface ProductRepository {
    Product save(Product product);
    void delete(String uuid);
    Product find(String uuid);
}
