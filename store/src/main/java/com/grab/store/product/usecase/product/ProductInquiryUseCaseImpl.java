package com.grab.store.product.usecase.product;

import com.grab.store_interface.product.dto.product.ReadableProduct;
import com.grab.store_interface.product.usecase.product.ProductInquiryUseCase;
import com.product.domain.entity.product.Product;
import com.product.domain.repository.product.ProductRepository;

import java.util.Optional;

public class ProductInquiryUseCaseImpl implements ProductInquiryUseCase {
    private final ProductRepository productRepository;

    public ProductInquiryUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Optional<ReadableProduct> findByUuid(String uuid) {
        Product product =  this.productRepository.find(uuid);
        return null;
    }
}
