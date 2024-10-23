package com.product.infrastructure.repository.product;

import com.product.domain.entity.product.Product;
import com.product.domain.repository.product.ProductRepository;

public class ProductFacadeRepository implements ProductRepository {
    @Override
    public Product save(Product product) {
        return null;
    }

    @Override
    public void delete(String uuid) {

    }

    @Override
    public Product find(String uuid) {
        return null;
    }
}
