package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.AddPriceToPriceListCommand;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.port.inbound.AddPriceToPriceListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddPriceToPriceListCommandHandler implements CommandHandler<AddPriceToPriceListCommand, PriceListResult> {

    private final AddPriceToPriceListUseCase addPriceToPriceListUseCase;

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
