package com.pricing.application.service;

import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.port.inbound.GetPriceSetUseCase;
import com.pricing.application.port.outbound.PriceQueryPort;
import com.pricing.application.model.read.GetPriceSetQuery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetPriceSetService implements GetPriceSetUseCase {

    private final PriceQueryPort priceQueryPort;

    public PriceSetResult execute(GetPriceSetQuery query) {
        return priceQueryPort.findPriceSetById(query.priceSetId().getValue())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceSetNotFound(query.priceSetId().getValue()),
                        "Price set not found"
                ));
    }
}
