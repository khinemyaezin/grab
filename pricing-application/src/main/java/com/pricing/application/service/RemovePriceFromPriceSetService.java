package com.pricing.application.service;

import com.pricing.application.port.inbound.RemovePriceFromPriceSetUseCase;

import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.model.write.RemovePriceFromPriceSetCommand;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.port.outbound.PriceSetRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class RemovePriceFromPriceSetService implements RemovePriceFromPriceSetUseCase {

    private final PriceSetRepository priceSetRepository;
    public PriceSetResult execute(RemovePriceFromPriceSetCommand command) {
        PriceSet priceSet = priceSetRepository.findById(command.priceSetId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceSetNotFound(command.priceSetId().getValue()),
                        "Price set not found"
                ));
        priceSet.removePrice(command.priceId(), Instant.now());
        PriceSet saved = priceSetRepository.save(priceSet);
        return PricingResultMapper.toPriceSetResult(saved);
    }
}
