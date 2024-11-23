package com.product.infrastructure.service;

import com.product.domain.entity.product.Product;
import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.entity.product.entity.ProductEntity;

import java.util.Optional;

public interface ProductEntityService {
    ProductEntity findOrCreateProduct(Product product, CategoryEntity categoryEntity);

    ProductEntity createNewProduct(Product product, CategoryEntity categoryEntity);

    ProductEntity save(ProductEntity productEntity);

    Optional<ProductEntity> find(String uuid);

    void delete(ProductEntity productEntity);
}
