package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.CreatePriceListCommand;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.port.inbound.CreatePriceListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreatePriceListCommandHandler implements CommandHandler<CreatePriceListCommand, PriceListResult> {

    private final CreatePriceListUseCase createPriceListUseCase;

    @Override
    @PricingTransactional
    public PriceListResult handle(CreatePriceListCommand command) {
        return createPriceListUseCase.execute(command);
    }

    @Override
    public Class<CreatePriceListCommand> getCommandType() {
        return CreatePriceListCommand.class;
    }
}
