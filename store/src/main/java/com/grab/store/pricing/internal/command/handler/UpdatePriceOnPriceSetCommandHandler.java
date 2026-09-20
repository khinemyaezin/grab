package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.PriceSetResult;
import com.pricing.application.model.write.UpdatePriceOnPriceSetCommand;
import com.pricing.application.port.inbound.UpdatePriceOnPriceSetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdatePriceOnPriceSetCommandHandler implements CommandHandler<UpdatePriceOnPriceSetCommand, PriceSetResult> {

    private final UpdatePriceOnPriceSetUseCase updatePriceOnPriceSetUseCase;

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
