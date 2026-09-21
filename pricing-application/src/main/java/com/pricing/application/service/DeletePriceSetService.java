package com.pricing.application.service;

import com.pricing.application.port.inbound.DeletePriceSetUseCase;

import com.pricing.application.model.write.DeletePriceSetCommand;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.domain.port.outbound.PriceSetRepository;
import com.pricing.domain.port.outbound.VariantPriceSetLinkRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeletePriceSetService implements DeletePriceSetUseCase {

    private final PriceSetRepository priceSetRepository;
    private final VariantPriceSetLinkRepository variantPriceSetLinkRepository;
    public Void execute(DeletePriceSetCommand command) {
        priceSetRepository.findById(command.priceSetId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceSetNotFound(command.priceSetId().getValue()),
                        "Price set not found"
                ));
        variantPriceSetLinkRepository.deleteByPriceSetId(command.priceSetId().getValue());
        priceSetRepository.delete(command.priceSetId());
        return null;
    }
}
