package com.product.infrastructure.service;

import com.product.domain.aggregate.product.Product;
import com.product.infrastructure.entity.product.entity.ProductEntity;

import java.util.Optional;

public interface ProductService {
    ProductEntity findOrBuildProduct(Product product);

    ProductEntity save(ProductEntity productEntity);

    Optional<ProductEntity> find(String uuid);

    void delete(ProductEntity productEntity);

    Boolean exists(String uuid);
}
