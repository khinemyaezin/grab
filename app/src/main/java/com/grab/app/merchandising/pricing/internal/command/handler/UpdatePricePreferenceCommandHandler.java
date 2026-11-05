package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.PricePreferenceResult;
import com.pricing.application.model.write.UpdatePricePreferenceCommand;
import com.pricing.application.port.inbound.UpdatePricePreferenceUseCase;
import org.springframework.stereotype.Component;

@Component
public class UpdatePricePreferenceCommandHandler
        implements CommandHandler<UpdatePricePreferenceCommand, PricePreferenceResult> {

    private final UpdatePricePreferenceUseCase updatePricePreferenceUseCase;

    public UpdatePricePreferenceCommandHandler(UpdatePricePreferenceUseCase updatePricePreferenceUseCase) {
        this.updatePricePreferenceUseCase = updatePricePreferenceUseCase;
    }

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

