package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.AddPriceToPriceSetCommand;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.port.inbound.AddPriceToPriceSetUseCase;
import org.springframework.stereotype.Component;

@Component
public class AddPriceToPriceSetCommandHandler implements CommandHandler<AddPriceToPriceSetCommand, PriceSetResult> {

    private final AddPriceToPriceSetUseCase addPriceToPriceSetUseCase;

    public AddPriceToPriceSetCommandHandler(AddPriceToPriceSetUseCase addPriceToPriceSetUseCase) {
        this.addPriceToPriceSetUseCase = addPriceToPriceSetUseCase;
    }

    @Override
    @PricingTransactional
    public PriceSetResult handle(AddPriceToPriceSetCommand command) {
        return addPriceToPriceSetUseCase.execute(command);
    }

    @Override
    public Class<AddPriceToPriceSetCommand> getCommandType() {
        return AddPriceToPriceSetCommand.class;
    }
}

