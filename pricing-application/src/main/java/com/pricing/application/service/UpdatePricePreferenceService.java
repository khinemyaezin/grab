package com.pricing.application.service;

import com.pricing.application.port.inbound.UpdatePricePreferenceUseCase;

import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.model.write.UpdatePricePreferenceCommand;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.util.PricingResultMapper;
import com.pricing.domain.aggregate.PricePreference;
import com.pricing.domain.port.outbound.PricePreferenceRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class UpdatePricePreferenceService implements UpdatePricePreferenceUseCase {

    private final PricePreferenceRepository pricePreferenceRepository;
    public PricePreferenceResult execute(UpdatePricePreferenceCommand command) {
        PricePreference preference = pricePreferenceRepository.findById(command.pricePreferenceId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PricePreferenceNotFound(command.pricePreferenceId().getValue()),
                        "Price preference not found"
                ));
        preference.update(command.attribute(), command.value(), command.taxInclusive(), Instant.now());
        PricePreference saved = pricePreferenceRepository.save(preference);
        return PricingResultMapper.toPreferenceResult(saved);
    }
}
