package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.RemovePriceFromPriceListCommand;
import com.pricing.application.port.inbound.RemovePriceFromPriceListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemovePriceFromPriceListCommandHandler implements CommandHandler<RemovePriceFromPriceListCommand, PriceListResult> {

    private final RemovePriceFromPriceListUseCase removePriceFromPriceListUseCase;

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
