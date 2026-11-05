package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.AddPriceToPriceListCommand;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.port.inbound.AddPriceToPriceListUseCase;
import org.springframework.stereotype.Component;

@Component
public class AddPriceToPriceListCommandHandler implements CommandHandler<AddPriceToPriceListCommand, PriceListResult> {

    private final AddPriceToPriceListUseCase addPriceToPriceListUseCase;

    public AddPriceToPriceListCommandHandler(AddPriceToPriceListUseCase addPriceToPriceListUseCase) {
        this.addPriceToPriceListUseCase = addPriceToPriceListUseCase;
    }

    @Override
    @PricingTransactional
    public PriceListResult handle(AddPriceToPriceListCommand command) {
        return addPriceToPriceListUseCase.execute(command);
    }

    @Override
    public Class<AddPriceToPriceListCommand> getCommandType() {
        return AddPriceToPriceListCommand.class;
    }
}

