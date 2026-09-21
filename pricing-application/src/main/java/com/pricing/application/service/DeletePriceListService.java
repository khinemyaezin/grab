package com.pricing.application.service;

import com.pricing.application.port.inbound.DeletePriceListUseCase;

import com.pricing.application.model.write.DeletePriceListCommand;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.domain.port.outbound.PriceListRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeletePriceListService implements DeletePriceListUseCase {

    private final PriceListRepository priceListRepository;
    public Void execute(DeletePriceListCommand command) {
        priceListRepository.findById(command.priceListId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceListNotFound(command.priceListId().getValue()),
                        "Price list not found"
                ));
        priceListRepository.delete(command.priceListId());
        return null;
    }
}
