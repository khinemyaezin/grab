package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.command.PriceSetResult;
import com.grab.store.pricing.internal.command.RemovePriceFromPriceSetCommand;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.repository.PriceSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class RemovePriceFromPriceSetCommandHandler
        implements CommandHandler<RemovePriceFromPriceSetCommand, PriceSetResult> {

    private final PriceSetRepository priceSetRepository;

    @Override
    @PricingTransactional
    public PriceSetResult handle(RemovePriceFromPriceSetCommand command) {
        PriceSet priceSet = priceSetRepository.findById(command.priceSetId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceSetNotFound(command.priceSetId().getValue()),
                        "Price set not found"
                ));
        priceSet.removePrice(command.priceId(), Instant.now());
        PriceSet saved = priceSetRepository.save(priceSet);
        return PricingResultMapper.toPriceSetResult(saved);
    }

    @Override
    public Class<RemovePriceFromPriceSetCommand> getCommandType() {
        return RemovePriceFromPriceSetCommand.class;
    }
}
