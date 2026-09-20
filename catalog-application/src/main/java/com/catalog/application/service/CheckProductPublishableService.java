package com.catalog.application.service;

import com.catalog.application.port.inbound.CheckProductPublishableUseCase;
import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.model.read.CheckProductPublishableQuery;
import com.catalog.application.model.read.CheckProductPublishableResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckProductPublishableService implements CheckProductPublishableUseCase {

    private final ProductQueryPort productQueryPort;

    public CheckProductPublishableResult execute(CheckProductPublishableQuery query) {
        return productQueryPort.findByIdAndMerchantId(query.productId(), query.merchantId())
                .map(view -> toResult(view.merchantId(), view.status(), query.merchantId()))
                .orElseGet(CheckProductPublishableResult::missing);
    }

    private CheckProductPublishableResult toResult(String productMerchantId, String status, String merchantId) {
        boolean owned = productMerchantId != null && productMerchantId.equals(merchantId);
        boolean active = "ACTIVE".equals(status);
        return new CheckProductPublishableResult(true, owned, active);
    }
}
