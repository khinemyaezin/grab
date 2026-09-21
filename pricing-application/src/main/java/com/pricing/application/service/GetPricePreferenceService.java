package com.pricing.application.service;

import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.port.inbound.GetPricePreferenceUseCase;
import com.pricing.application.port.outbound.PriceQueryPort;
import com.pricing.application.model.read.GetPricePreferenceQuery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetPricePreferenceService implements GetPricePreferenceUseCase {

    private final PriceQueryPort priceQueryPort;

    public PricePreferenceResult execute(GetPricePreferenceQuery query) {
        return priceQueryPort.findPricePreferenceById(query.pricePreferenceId().getValue())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PricePreferenceNotFound(query.pricePreferenceId().getValue()),
                        "Price preference not found"
                ));
    }
}
