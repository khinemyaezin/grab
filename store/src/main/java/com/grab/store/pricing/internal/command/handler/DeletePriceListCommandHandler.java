package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.command.DeletePriceListCommand;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.grab.store.pricing.internal.exception.PricingServiceError;
import com.grab.store.pricing.internal.exception.PricingServiceException;
import com.pricing.domain.repository.PriceListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class DeletePriceListCommandHandler implements CommandHandler<DeletePriceListCommand, Void> {

    private final PriceListRepository priceListRepository;

    @Override
    @PricingTransactional
    public Void handle(DeletePriceListCommand command) {
        priceListRepository.findById(command.priceListId())
                .orElseThrow(() -> new PricingServiceException(
                        new PricingServiceError.PriceListNotFound(command.priceListId().getValue()),
                        "Price list not found"
                ));
        priceListRepository.delete(command.priceListId());
        return null;
    }

    @Override
    public Class<DeletePriceListCommand> getCommandType() {
        return DeletePriceListCommand.class;
    }
}
