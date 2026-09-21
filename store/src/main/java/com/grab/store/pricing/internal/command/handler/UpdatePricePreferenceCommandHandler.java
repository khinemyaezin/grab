package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.model.write.UpdatePricePreferenceCommand;
import com.pricing.application.port.inbound.UpdatePricePreferenceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdatePricePreferenceCommandHandler implements CommandHandler<UpdatePricePreferenceCommand, PricePreferenceResult> {

    private final UpdatePricePreferenceUseCase updatePricePreferenceUseCase;

    @Override
    @PricingTransactional
    public PricePreferenceResult handle(UpdatePricePreferenceCommand command) {
        return updatePricePreferenceUseCase.execute(command);
    }

    @Override
    public Class<UpdatePricePreferenceCommand> getCommandType() {
        return UpdatePricePreferenceCommand.class;
    }
}
