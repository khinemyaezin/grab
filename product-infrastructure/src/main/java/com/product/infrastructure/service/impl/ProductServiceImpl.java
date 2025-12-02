package com.product.infrastructure.service.impl;

import com.product.domain.aggregate.product.Product;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.factory.ProductEntityFactory;
import com.product.infrastructure.mapper.product.ProductEntityMapper;
import com.product.infrastructure.repository.jpa.ProductJpaRepository;
import com.product.infrastructure.service.ProductService;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductJpaRepository productJpaRepository;
    private final ProductEntityMapper productEntityMapper;
    private final ProductEntityFactory productEntityFactory;

    @Override
    public ProductEntity findOrBuildProduct(Product product) {
        Optional<ProductEntity> optionalProduct = productJpaRepository.findByUuid(product.getId().getValue());
        if (optionalProduct.isPresent()) {
            ProductEntity existing = optionalProduct.get();
            productEntityMapper.map(product, existing);
            return existing;
        } else {
            return productEntityFactory.create(product);
        }
    }

    @Override
    public ProductEntity save(ProductEntity productEntity) {
        return this.productJpaRepository.save(productEntity);
    }

    @Override
    public Optional<ProductEntity> find(String uuid) {
        return this.productJpaRepository.findByUuid(uuid);
    }

    @Override
    public void delete(ProductEntity productEntity) {
        this.productJpaRepository.delete(productEntity);
    }

    @Override
    public Boolean exists(String uuid) {
        return this.productJpaRepository.existsById(uuid);
    }
}
