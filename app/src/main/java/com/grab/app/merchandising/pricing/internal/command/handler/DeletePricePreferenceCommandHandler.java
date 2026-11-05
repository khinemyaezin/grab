package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.DeletePricePreferenceCommand;
import com.pricing.application.port.inbound.DeletePricePreferenceUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeletePricePreferenceCommandHandler implements CommandHandler<DeletePricePreferenceCommand, Void> {

    private final DeletePricePreferenceUseCase deletePricePreferenceUseCase;

    public DeletePricePreferenceCommandHandler(DeletePricePreferenceUseCase deletePricePreferenceUseCase) {
        this.deletePricePreferenceUseCase = deletePricePreferenceUseCase;
    }

    @Override
    @PricingTransactional
    public Void handle(DeletePricePreferenceCommand command) {
        return deletePricePreferenceUseCase.execute(command);
    }

    @Override
    public Class<DeletePricePreferenceCommand> getCommandType() {
        return DeletePricePreferenceCommand.class;
    }
}

