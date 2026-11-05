package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.RemovePriceFromPriceListCommand;
import com.pricing.application.port.inbound.RemovePriceFromPriceListUseCase;
import org.springframework.stereotype.Component;

@Component
public class RemovePriceFromPriceListCommandHandler
        implements CommandHandler<RemovePriceFromPriceListCommand, PriceListResult> {

    private final RemovePriceFromPriceListUseCase removePriceFromPriceListUseCase;

    public RemovePriceFromPriceListCommandHandler(
            RemovePriceFromPriceListUseCase removePriceFromPriceListUseCase) {
        this.removePriceFromPriceListUseCase = removePriceFromPriceListUseCase;
    }

    @Override
    @PricingTransactional
    public PriceListResult handle(RemovePriceFromPriceListCommand command) {
        return removePriceFromPriceListUseCase.execute(command);
    }

    @Override
    public Class<RemovePriceFromPriceListCommand> getCommandType() {
        return RemovePriceFromPriceListCommand.class;
    }
}

