package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.pricing.internal.command.CreatePriceSetCommand;
import com.grab.store.pricing.internal.command.CreatePriceSetResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.domain.aggregate.PriceSet;
import com.pricing.domain.repository.PriceSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class CreatePriceSetCommandHandler
        implements CommandHandler<CreatePriceSetCommand, CreatePriceSetResult> {

    private final PriceSetRepository priceSetRepository;
    private final IdGenerator idGenerator;

    @Override
    @PricingTransactional
    public CreatePriceSetResult handle(CreatePriceSetCommand command) {
        PriceSet priceSet = PriceSet.create(idGenerator.generateId(), Instant.now());
        PriceSet saved = priceSetRepository.save(priceSet);
        return new CreatePriceSetResult(saved.getId().getValue());
    }

    @Override
    public Class<CreatePriceSetCommand> getCommandType() {
        return CreatePriceSetCommand.class;
    }
}
