package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.CreatePriceSetCommand;
import com.pricing.application.model.write.CreatePriceSetResult;
import com.pricing.application.port.inbound.CreatePriceSetUseCase;
import org.springframework.stereotype.Component;

@Component
public class CreatePriceSetCommandHandler implements CommandHandler<CreatePriceSetCommand, CreatePriceSetResult> {

    private final CreatePriceSetUseCase createPriceSetUseCase;

    public CreatePriceSetCommandHandler(CreatePriceSetUseCase createPriceSetUseCase) {
        this.createPriceSetUseCase = createPriceSetUseCase;
    }

    @Override
    @PricingTransactional
    public CreatePriceSetResult handle(CreatePriceSetCommand command) {
        return createPriceSetUseCase.execute(command);
    }

    @Override
    public Class<CreatePriceSetCommand> getCommandType() {
        return CreatePriceSetCommand.class;
    }
}

