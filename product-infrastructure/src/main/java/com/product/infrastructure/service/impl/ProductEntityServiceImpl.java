package com.product.infrastructure.service.impl;

import com.product.domain.entity.product.Product;
import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.mapper.product.ProductEntityMapper;
import com.product.infrastructure.repository.product.ProductEntityRepository;
import com.product.infrastructure.service.ProductEntityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductEntityServiceImpl implements ProductEntityService {
    private final ProductEntityRepository productEntityRepository;
    private final ProductEntityMapper productEntityMapper;

    @Override
    public ProductEntity findOrCreateProduct(Product product, CategoryEntity categoryEntity) {
        return productEntityRepository.findByUuid(product.getId())
                .map(existingProductEntity -> {
                    productEntityMapper.map(product, categoryEntity, existingProductEntity);
                    return existingProductEntity;
                }).orElseGet(() -> createNewProduct(product, categoryEntity));
    }

    @Override
    public ProductEntity createNewProduct(Product product, CategoryEntity categoryEntity) {
        ProductEntity productEntity = new ProductEntity();
        productEntityMapper.map(product, categoryEntity, productEntity);
        return productEntity;
    }

    @Override
    @Transactional
    public ProductEntity save(ProductEntity productEntity){
        return this.productEntityRepository.save(productEntity);
    }

    @Transactional(readOnly = true)
    public Optional<ProductEntity> find(String uuid) {
        return this.productEntityRepository.findByUuid(uuid);
    }

    @Transactional
    @Override
    public void delete(ProductEntity productEntity) {
        this.productEntityRepository.delete(productEntity);
    }


}
