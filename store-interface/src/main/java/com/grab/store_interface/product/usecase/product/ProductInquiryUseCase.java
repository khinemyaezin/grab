package com.grab.store_interface.product.usecase.product;

import com.grab.store_interface.product.dto.product.ReadableProduct;

import java.util.Optional;

public interface ProductInquiryUseCase {
    Optional<ReadableProduct> findByUuid(String uuid);
}
