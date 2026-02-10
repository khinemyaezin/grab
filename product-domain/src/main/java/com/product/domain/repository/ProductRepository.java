package com.product.domain.repository;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;

import java.util.Optional;

public interface ProductRepository{
    void save(Product product);
    void delete(Product product);
    Optional<Product> find(Id productId);
}
