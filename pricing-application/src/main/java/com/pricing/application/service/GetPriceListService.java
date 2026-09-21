package com.pricing.application.service;

import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.port.inbound.GetPriceListUseCase;
import com.pricing.application.port.outbound.PriceQueryPort;
import com.pricing.application.model.read.GetPriceListQuery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetPriceListService implements GetPriceListUseCase {

    private final PriceQueryPort priceQueryPort;

    public PriceListResult execute(GetPriceListQuery query) {
        return priceQueryPort.findPriceListById(query.priceListId().getValue())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceListNotFound(query.priceListId().getValue()),
                        "Price list not found"
                ));
    }
}
