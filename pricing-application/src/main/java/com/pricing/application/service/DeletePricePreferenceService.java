package com.pricing.application.service;

import com.pricing.application.port.inbound.DeletePricePreferenceUseCase;

import com.pricing.application.model.write.DeletePricePreferenceCommand;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.domain.port.outbound.PricePreferenceRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeletePricePreferenceService implements DeletePricePreferenceUseCase {

    private final PricePreferenceRepository pricePreferenceRepository;
    public Void execute(DeletePricePreferenceCommand command) {
        pricePreferenceRepository.findById(command.pricePreferenceId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PricePreferenceNotFound(command.pricePreferenceId().getValue()),
                        "Price preference not found"
                ));
        pricePreferenceRepository.delete(command.pricePreferenceId());
        return null;
    }
}
