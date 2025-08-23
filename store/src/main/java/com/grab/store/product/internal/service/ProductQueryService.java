package com.grab.store.product.internal.service;

import com.product.domain.aggregate.product.Product;
import com.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductQueryService {
    private final ProductRepository productRepository;

    public ProductQueryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<Product> find(String id) {
        return productRepository.find(id);
    }
}
