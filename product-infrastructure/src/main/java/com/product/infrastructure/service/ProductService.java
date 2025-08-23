package com.product.infrastructure.service;

import com.product.domain.aggregate.product.Product;
import com.product.infrastructure.entity.category.entity.CategoryEntity;
import com.product.infrastructure.entity.product.entity.ProductEntity;

import java.util.Optional;

public interface ProductService {
    ProductEntity findOrBuildProduct(Product product, CategoryEntity categoryEntity);

    ProductEntity save(ProductEntity productEntity);

    Optional<ProductEntity> find(String uuid);

    void delete(ProductEntity productEntity);

    Boolean exists(String uuid);
}
