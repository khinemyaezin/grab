package com.pricing.application.service;

import com.pricing.application.port.inbound.CreateVariantPriceAssignmentUseCase;

import com.grab.framework.id.IdGenerator;
import com.pricing.application.model.write.CreateVariantPriceAssignmentCommand;
import com.pricing.application.model.write.CreateVariantPriceAssignmentResult;
import com.pricing.application.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.entity.Price;
import com.pricing.domain.port.outbound.PriceSetRepository;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.MoneyAmount;
import com.pricing.domain.port.outbound.VariantPriceSetLinkRepository;
import com.pricing.domain.valueobject.VariantPriceSetLink;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class CreateVariantPriceAssignmentService implements CreateVariantPriceAssignmentUseCase {

    private final PriceSetRepository priceSetRepository;
    private final VariantPriceSetLinkRepository variantPriceSetLinkRepository;
    private final IdGenerator idGenerator;
    public CreateVariantPriceAssignmentResult execute(CreateVariantPriceAssignmentCommand command) {
        Instant now = Instant.now();
        PriceSet priceSet = PriceSet.create(idGenerator.generateId(), now);
        Price price = Price.createBase(
                idGenerator.generateId(),
                priceSet.getId(),
                command.title(),
                CurrencyCode.of(command.currencyCode()),
                MoneyAmount.of(command.amount()),
                command.minQuantity(),
                command.maxQuantity(),
                PricingResultMapper.toRules(idGenerator, command.rules())
        );
        priceSet.addPrice(price, now);
        PriceSet saved = priceSetRepository.save(priceSet);
        String priceSetId = saved.getId().getValue();
        variantPriceSetLinkRepository.save(new VariantPriceSetLink(
                command.variantId(),
                priceSetId,
                command.productId(),
                command.sku(),
                command.merchantId(),
                now,
                now
        ));
        return new CreateVariantPriceAssignmentResult(priceSetId);
    }
}
