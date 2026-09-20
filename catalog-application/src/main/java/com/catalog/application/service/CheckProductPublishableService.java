package com.catalog.application.service;

import com.catalog.application.port.inbound.CheckProductPublishableUseCase;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.port.outbound.ProductRepository;
import com.catalog.domain.valueobject.ProductStatus;
import com.grab.framework.id.IdGenerator;
import com.catalog.application.query.CheckProductPublishableQuery;
import com.catalog.application.query.CheckProductPublishableResult;
import lombok.RequiredArgsConstructor;

@lombok.RequiredArgsConstructor
public class CheckProductPublishableService implements CheckProductPublishableUseCase {

    private final ProductRepository productRepository;
    private final IdGenerator idGenerator;

        public CheckProductPublishableResult execute(CheckProductPublishableQuery query) {
        return productRepository.find(idGenerator.convertIdFrom(query.productId()))
                .map(product -> toResult(product, query.merchantId()))
                .orElseGet(CheckProductPublishableResult::missing);
    }

    private CheckProductPublishableResult toResult(Product product, String merchantId) {
        boolean owned = product.getMerchantId() != null && product.getMerchantId().getValue().equals(merchantId);
        boolean active = product.getStatus() == ProductStatus.ACTIVE;
        return new CheckProductPublishableResult(true, owned, active);
    }
}
