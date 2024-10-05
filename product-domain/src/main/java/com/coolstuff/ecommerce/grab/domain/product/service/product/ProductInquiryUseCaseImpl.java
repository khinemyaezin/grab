package com.coolstuff.ecommerce.grab.domain.product.service.product;

import com.coolstuff.ecommerce.grab.domain.product.entity.product.ReadableProduct;
import com.coolstuff.ecommerce.grab.domain.product.repository.product.ProductRepository;

import java.util.Optional;

public class ProductInquiryUseCaseImpl implements ProductInquiryUseCase {
    private final ProductRepository productRepository;

    public ProductInquiryUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Optional<ReadableProduct> findByUuid(String uuid) {
        return this.productRepository.findByUuid(uuid);
    }
}
