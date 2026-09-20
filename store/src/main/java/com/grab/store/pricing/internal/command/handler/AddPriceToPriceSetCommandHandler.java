package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.AddPriceToPriceSetCommand;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.port.inbound.AddPriceToPriceSetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddPriceToPriceSetCommandHandler implements CommandHandler<AddPriceToPriceSetCommand, PriceSetResult> {

    private final AddPriceToPriceSetUseCase addPriceToPriceSetUseCase;

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
