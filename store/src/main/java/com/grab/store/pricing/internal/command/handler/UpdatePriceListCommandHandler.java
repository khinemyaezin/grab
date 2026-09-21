package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.UpdatePriceListCommand;
import com.pricing.application.port.inbound.UpdatePriceListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdatePriceListCommandHandler implements CommandHandler<UpdatePriceListCommand, PriceListResult> {

    private final UpdatePriceListUseCase updatePriceListUseCase;

    @Override
    @PricingTransactional
    public PriceListResult handle(UpdatePriceListCommand command) {
        return updatePriceListUseCase.execute(command);
    }

    @Override
    public Class<UpdatePriceListCommand> getCommandType() {
        return UpdatePriceListCommand.class;
    }
}
