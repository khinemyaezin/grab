package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.CreatePricePreferenceCommand;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.port.inbound.CreatePricePreferenceUseCase;
import org.springframework.stereotype.Component;

@Component
public class CreatePricePreferenceCommandHandler
        implements CommandHandler<CreatePricePreferenceCommand, PricePreferenceResult> {

    private final CreatePricePreferenceUseCase createPricePreferenceUseCase;

    public CreatePricePreferenceCommandHandler(CreatePricePreferenceUseCase createPricePreferenceUseCase) {
        this.createPricePreferenceUseCase = createPricePreferenceUseCase;
    }

    @Override
    @PricingTransactional
    public PricePreferenceResult handle(CreatePricePreferenceCommand command) {
        return createPricePreferenceUseCase.execute(command);
    }

    @Override
    public Class<CreatePricePreferenceCommand> getCommandType() {
        return CreatePricePreferenceCommand.class;
    }
}

