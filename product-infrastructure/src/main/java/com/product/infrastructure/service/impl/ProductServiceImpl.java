package com.product.infrastructure.service.impl;

import com.product.domain.aggregate.product.Product;
import com.product.infrastructure.entity.category.entity.CategoryEntity;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.factory.ProductEntityFactory;
import com.product.infrastructure.mapper.product.ProductEntityMapper;
import com.product.infrastructure.repository.jpa.ProductJpaRepository;
import com.product.infrastructure.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductJpaRepository productJpaRepository;
    private final ProductEntityMapper productEntityMapper;
    private final ProductEntityFactory productEntityFactory;

    @Override
    public ProductEntity findOrBuildProduct(Product product, CategoryEntity categoryEntity) {
        return product.getId()
                .flatMap( id-> productJpaRepository.findByUuid(id.getValue()))
                .map(existingProductEntity -> {
                    productEntityMapper.map(product, categoryEntity, existingProductEntity);
                    return existingProductEntity;
                })
                .orElseGet(() -> productEntityFactory.create(product, categoryEntity));
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
