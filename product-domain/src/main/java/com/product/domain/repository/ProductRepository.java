package com.product.domain.repository;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;

public interface ProductRepository{
    void save(Product product);
    void delete(Product product);
    Product find(Id productId);
}
