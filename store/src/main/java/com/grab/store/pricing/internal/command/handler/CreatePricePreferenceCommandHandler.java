package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.CreatePricePreferenceCommand;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.port.inbound.CreatePricePreferenceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreatePricePreferenceCommandHandler implements CommandHandler<CreatePricePreferenceCommand, PricePreferenceResult> {

    private final CreatePricePreferenceUseCase createPricePreferenceUseCase;

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
