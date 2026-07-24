package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.command.DeletePricePreferenceCommand;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.pricing.domain.repository.PricePreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class DeletePricePreferenceCommandHandler
        implements CommandHandler<DeletePricePreferenceCommand, Void> {

    private final PricePreferenceRepository pricePreferenceRepository;

    @Override
    @PricingTransactional
    public Void handle(DeletePricePreferenceCommand command) {
        pricePreferenceRepository.findById(command.pricePreferenceId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PricePreferenceNotFound(command.pricePreferenceId().getValue()),
                        "Price preference not found"
                ));
        pricePreferenceRepository.delete(command.pricePreferenceId());
        return null;
    }

    @Override
    public Class<DeletePricePreferenceCommand> getCommandType() {
        return DeletePricePreferenceCommand.class;
    }
}
