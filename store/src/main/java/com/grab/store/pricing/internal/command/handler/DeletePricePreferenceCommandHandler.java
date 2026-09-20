package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.DeletePricePreferenceCommand;
import com.pricing.application.port.inbound.DeletePricePreferenceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeletePricePreferenceCommandHandler implements CommandHandler<DeletePricePreferenceCommand, Void> {

    private final DeletePricePreferenceUseCase deletePricePreferenceUseCase;

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
