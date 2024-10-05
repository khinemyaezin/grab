package com.coolstuff.ecommerce.grab.domain.product.service.product;

import com.coolstuff.ecommerce.grab.domain.product.entity.product.ReadableProduct;

import java.util.Optional;

public interface ProductInquiryUseCase {
    Optional<ReadableProduct> findByUuid(String uuid);
}
