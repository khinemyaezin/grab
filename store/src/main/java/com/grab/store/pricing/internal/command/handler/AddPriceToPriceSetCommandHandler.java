package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.pricing.internal.command.AddPriceToPriceSetCommand;
import com.grab.store.pricing.internal.command.PriceSetResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.entity.Price;
import com.pricing.domain.repository.PriceSetRepository;
import com.pricing.domain.valueobject.CurrencyCode;
import com.pricing.domain.valueobject.MoneyAmount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class AddPriceToPriceSetCommandHandler
        implements CommandHandler<AddPriceToPriceSetCommand, PriceSetResult> {

    private final PriceSetRepository priceSetRepository;
    private final IdGenerator idGenerator;

    @Override
    @PricingTransactional
    public PriceSetResult handle(AddPriceToPriceSetCommand command) {
        PriceSet priceSet = priceSetRepository.findById(command.priceSetId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceSetNotFound(command.priceSetId().getValue()),
                        "Price set not found"
                ));
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
        Instant now = Instant.now();
        priceSet.addPrice(price, now);
        PriceSet saved = priceSetRepository.save(priceSet);
        return PricingResultMapper.toPriceSetResult(saved);
    }

    @Override
    public Class<AddPriceToPriceSetCommand> getCommandType() {
        return AddPriceToPriceSetCommand.class;
    }
}
