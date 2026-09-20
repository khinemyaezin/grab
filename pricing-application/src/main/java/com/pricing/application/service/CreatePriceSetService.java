package com.pricing.application.service;

import com.pricing.application.port.inbound.CreatePriceSetUseCase;

import com.grab.framework.id.IdGenerator;
import com.pricing.application.model.write.CreatePriceSetCommand;
import com.pricing.application.model.write.CreatePriceSetResult;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.port.outbound.PriceSetRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class CreatePriceSetService implements CreatePriceSetUseCase {

    private final PriceSetRepository priceSetRepository;
    private final IdGenerator idGenerator;
    public CreatePriceSetResult execute(CreatePriceSetCommand command) {
        PriceSet priceSet = PriceSet.create(idGenerator.generateId(), Instant.now());
        PriceSet saved = priceSetRepository.save(priceSet);
        return new CreatePriceSetResult(saved.getId().getValue());
    }
}
