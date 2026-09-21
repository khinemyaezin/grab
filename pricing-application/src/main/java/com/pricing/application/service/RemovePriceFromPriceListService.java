package com.pricing.application.service;

import com.pricing.application.port.inbound.RemovePriceFromPriceListUseCase;

import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.RemovePriceFromPriceListCommand;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceList;
import com.pricing.domain.port.outbound.PriceListRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class RemovePriceFromPriceListService implements RemovePriceFromPriceListUseCase {

    private final PriceListRepository priceListRepository;
    public PriceListResult execute(RemovePriceFromPriceListCommand command) {
        PriceList priceList = priceListRepository.findById(command.priceListId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceListNotFound(command.priceListId().getValue()),
                        "Price list not found"
                ));
        priceList.removePrice(command.priceId(), Instant.now());
        PriceList saved = priceListRepository.save(priceList);
        return PricingResultMapper.toPriceListResult(saved);
    }
}
