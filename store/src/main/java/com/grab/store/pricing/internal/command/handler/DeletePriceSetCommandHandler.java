package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.command.DeletePriceSetCommand;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.pricing.domain.repository.PriceSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class DeletePriceSetCommandHandler implements CommandHandler<DeletePriceSetCommand, Void> {

    private final PriceSetRepository priceSetRepository;

    @Override
    @PricingTransactional
    public Void handle(DeletePriceSetCommand command) {
        priceSetRepository.findById(command.priceSetId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceSetNotFound(command.priceSetId().getValue()),
                        "Price set not found"
                ));
        priceSetRepository.delete(command.priceSetId());
        return null;
    }

    @Override
    public Class<DeletePriceSetCommand> getCommandType() {
        return DeletePriceSetCommand.class;
    }
}
