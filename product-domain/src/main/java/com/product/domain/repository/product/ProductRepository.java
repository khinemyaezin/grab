package com.product.domain.repository.product;

import com.product.domain.entity.product.Product;

import java.util.Optional;

public interface ProductRepository{
    Product save(Product product);
    void delete(String uuid);
    Optional<Product> find(String uuid);
}
