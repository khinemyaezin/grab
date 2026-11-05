package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.PriceListResult;
import com.pricing.application.model.write.UpdatePriceListCommand;
import com.pricing.application.port.inbound.UpdatePriceListUseCase;
import org.springframework.stereotype.Component;

@Component
public class UpdatePriceListCommandHandler implements CommandHandler<UpdatePriceListCommand, PriceListResult> {

    private final UpdatePriceListUseCase updatePriceListUseCase;

    public UpdatePriceListCommandHandler(UpdatePriceListUseCase updatePriceListUseCase) {
        this.updatePriceListUseCase = updatePriceListUseCase;
    }

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

