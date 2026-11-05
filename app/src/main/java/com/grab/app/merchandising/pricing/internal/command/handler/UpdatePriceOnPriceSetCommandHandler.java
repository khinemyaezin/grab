package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.model.write.UpdatePriceOnPriceSetCommand;
import com.pricing.application.port.inbound.UpdatePriceOnPriceSetUseCase;
import org.springframework.stereotype.Component;

@Component
public class UpdatePriceOnPriceSetCommandHandler
        implements CommandHandler<UpdatePriceOnPriceSetCommand, PriceSetResult> {

    private final UpdatePriceOnPriceSetUseCase updatePriceOnPriceSetUseCase;

    public UpdatePriceOnPriceSetCommandHandler(UpdatePriceOnPriceSetUseCase updatePriceOnPriceSetUseCase) {
        this.updatePriceOnPriceSetUseCase = updatePriceOnPriceSetUseCase;
    }

    @Override
    @PricingTransactional
    public PriceSetResult handle(UpdatePriceOnPriceSetCommand command) {
        return updatePriceOnPriceSetUseCase.execute(command);
    }

    @Override
    public Class<UpdatePriceOnPriceSetCommand> getCommandType() {
        return UpdatePriceOnPriceSetCommand.class;
    }
}

