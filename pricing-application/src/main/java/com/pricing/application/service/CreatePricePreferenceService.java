package com.pricing.application.service;

import com.pricing.application.port.inbound.CreatePricePreferenceUseCase;

import com.grab.framework.id.IdGenerator;
import com.pricing.application.model.write.CreatePricePreferenceCommand;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.util.PricingResultMapper;
import com.pricing.domain.aggregate.PricePreference;
import com.pricing.domain.port.outbound.PricePreferenceRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class CreatePricePreferenceService implements CreatePricePreferenceUseCase {

    private final PricePreferenceRepository pricePreferenceRepository;
    private final IdGenerator idGenerator;
    public PricePreferenceResult execute(CreatePricePreferenceCommand command) {
        PricePreference preference = PricePreference.create(
                idGenerator.generateId(),
                command.attribute(),
                command.value(),
                command.taxInclusive(),
                Instant.now()
        );
        PricePreference saved = pricePreferenceRepository.save(preference);
        return PricingResultMapper.toPreferenceResult(saved);
    }
}
