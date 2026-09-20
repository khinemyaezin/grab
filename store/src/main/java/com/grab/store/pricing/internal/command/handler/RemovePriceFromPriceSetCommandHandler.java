package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.model.write.RemovePriceFromPriceSetCommand;
import com.pricing.application.port.inbound.RemovePriceFromPriceSetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemovePriceFromPriceSetCommandHandler implements CommandHandler<RemovePriceFromPriceSetCommand, PriceSetResult> {

    private final RemovePriceFromPriceSetUseCase removePriceFromPriceSetUseCase;

    @Override
    @PricingTransactional
    public PriceSetResult handle(RemovePriceFromPriceSetCommand command) {
        return removePriceFromPriceSetUseCase.execute(command);
    }

    @Override
    public Class<RemovePriceFromPriceSetCommand> getCommandType() {
        return RemovePriceFromPriceSetCommand.class;
    }
}
