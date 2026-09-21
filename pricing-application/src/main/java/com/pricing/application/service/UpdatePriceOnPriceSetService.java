package com.pricing.application.service;

import com.pricing.application.port.inbound.UpdatePriceOnPriceSetUseCase;

import com.grab.framework.id.IdGenerator;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.model.write.UpdatePriceOnPriceSetCommand;
import com.pricing.application.exception.PricingServiceError;
import com.pricing.application.exception.PricingServiceException;
import com.pricing.application.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.entity.Price;
import com.pricing.domain.port.outbound.PriceSetRepository;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.MoneyAmount;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class UpdatePriceOnPriceSetService implements UpdatePriceOnPriceSetUseCase {

    private final PriceSetRepository priceSetRepository;
    private final IdGenerator idGenerator;
    public PriceSetResult execute(UpdatePriceOnPriceSetCommand command) {
        PriceSet priceSet = priceSetRepository.findById(command.priceSetId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceSetNotFound(command.priceSetId().getValue()),
                        "Price set not found"
                ));
        Price existing = priceSet.findPrice(command.priceId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceSetNotFound(command.priceId().getValue()),
                        "Price not found"
                ));
        Price updated = Price.createBase(
                existing.getId(),
                priceSet.getId(),
                command.title(),
                CurrencyCode.of(command.currencyCode()),
                MoneyAmount.of(command.amount()),
                command.minQuantity(),
                command.maxQuantity(),
                PricingResultMapper.toRules(idGenerator, command.rules())
        );
        Instant now = Instant.now();
        priceSet.replacePrice(updated, now);
        PriceSet saved = priceSetRepository.save(priceSet);
        return PricingResultMapper.toPriceSetResult(saved);
    }
}
