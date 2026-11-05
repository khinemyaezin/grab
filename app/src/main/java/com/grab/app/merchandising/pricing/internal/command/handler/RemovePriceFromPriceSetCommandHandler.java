package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.model.write.RemovePriceFromPriceSetCommand;
import com.pricing.application.port.inbound.RemovePriceFromPriceSetUseCase;
import org.springframework.stereotype.Component;

@Component
public class RemovePriceFromPriceSetCommandHandler
        implements CommandHandler<RemovePriceFromPriceSetCommand, PriceSetResult> {

    private final RemovePriceFromPriceSetUseCase removePriceFromPriceSetUseCase;

    public RemovePriceFromPriceSetCommandHandler(RemovePriceFromPriceSetUseCase removePriceFromPriceSetUseCase) {
        this.removePriceFromPriceSetUseCase = removePriceFromPriceSetUseCase;
    }

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

