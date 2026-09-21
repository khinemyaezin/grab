package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.CreatePriceSetCommand;
import com.pricing.application.model.write.CreatePriceSetResult;
import com.pricing.application.port.inbound.CreatePriceSetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreatePriceSetCommandHandler implements CommandHandler<CreatePriceSetCommand, CreatePriceSetResult> {

    private final CreatePriceSetUseCase createPriceSetUseCase;

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
