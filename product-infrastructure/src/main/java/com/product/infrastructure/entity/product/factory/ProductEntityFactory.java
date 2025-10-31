package com.product.infrastructure.entity.product.factory;

import com.product.domain.aggregate.product.Product;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.mapper.product.ProductEntityMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProductEntityFactory {
    private final ProductEntityMapper productEntityMapper;

    public ProductEntity create(Product product) {
        ProductEntity productEntity = new ProductEntity();
        productEntityMapper.map(product, productEntity);
        return productEntity;
    }
}
