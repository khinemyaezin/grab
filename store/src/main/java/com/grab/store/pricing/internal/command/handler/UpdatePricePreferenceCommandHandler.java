package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.command.PricePreferenceResult;
import com.grab.store.pricing.internal.command.UpdatePricePreferenceCommand;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.grab.store.pricing.internal.util.PricingResultMapper;
import com.pricing.domain.aggregate.PricePreference;
import com.pricing.domain.repository.PricePreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class UpdatePricePreferenceCommandHandler
        implements CommandHandler<UpdatePricePreferenceCommand, PricePreferenceResult> {

    private final PricePreferenceRepository pricePreferenceRepository;

    @Override
    @PricingTransactional
    public PricePreferenceResult handle(UpdatePricePreferenceCommand command) {
        PricePreference preference = pricePreferenceRepository.findById(command.pricePreferenceId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PricePreferenceNotFound(command.pricePreferenceId().getValue()),
                        "Price preference not found"
                ));
        preference.update(command.attribute(), command.value(), command.taxInclusive(), Instant.now());
        PricePreference saved = pricePreferenceRepository.save(preference);
        return PricingResultMapper.toPreferenceResult(saved);
    }

    @Override
    public Class<UpdatePricePreferenceCommand> getCommandType() {
        return UpdatePricePreferenceCommand.class;
    }
}
