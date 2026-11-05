package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.CreatePriceListCommand;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.port.inbound.CreatePriceListUseCase;
import org.springframework.stereotype.Component;

@Component
public class CreatePriceListCommandHandler implements CommandHandler<CreatePriceListCommand, PriceListResult> {

    private final CreatePriceListUseCase createPriceListUseCase;

    public CreatePriceListCommandHandler(CreatePriceListUseCase createPriceListUseCase) {
        this.createPriceListUseCase = createPriceListUseCase;
    }

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

