package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.pricing.internal.command.CreatePricePreferenceCommand;
import com.grab.store.pricing.internal.command.PricePreferenceResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.aggregate.PricePreference;
import com.pricing.domain.repository.PricePreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class CreatePricePreferenceCommandHandler
        implements CommandHandler<CreatePricePreferenceCommand, PricePreferenceResult> {

    private final PricePreferenceRepository pricePreferenceRepository;
    private final IdGenerator idGenerator;

    @Override
    @PricingTransactional
    public PricePreferenceResult handle(CreatePricePreferenceCommand command) {
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

    @Override
    public Class<CreatePricePreferenceCommand> getCommandType() {
        return CreatePricePreferenceCommand.class;
    }
}
