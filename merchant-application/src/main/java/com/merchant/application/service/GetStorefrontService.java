package com.merchant.application.service;

import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.exception.MerchantServiceError;
import com.merchant.application.exception.MerchantServiceException;
import com.merchant.application.port.inbound.GetStorefrontUseCase;
import com.merchant.application.port.outbound.StorefrontQueryPort;
import com.merchant.application.model.read.GetStorefrontQuery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetStorefrontService implements GetStorefrontUseCase {
    private final StorefrontQueryPort storefrontQueryPort;

    public StorefrontResult execute(GetStorefrontQuery query) {
        return storefrontQueryPort.findById(query.storefrontId().getValue())
                .filter(view -> query.merchantId().getValue().equals(view.merchantId()))
                .map(StorefrontResult::from)
                .orElseThrow(() -> notFound(query.storefrontId().getValue()));
    }

    private MerchantServiceException notFound(String storefrontId) {
        return new MerchantServiceException(
                new MerchantServiceError.StorefrontNotFound(storefrontId),
                "Storefront not found"
        );
    }
}
