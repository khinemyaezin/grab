package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.DeletePriceSetCommand;
import com.pricing.application.port.inbound.DeletePriceSetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeletePriceSetCommandHandler implements CommandHandler<DeletePriceSetCommand, Void> {

    private final DeletePriceSetUseCase deletePriceSetUseCase;

    @Override
    @PricingTransactional
    public Void handle(DeletePriceSetCommand command) {
        return deletePriceSetUseCase.execute(command);
    }

    @Override
    public Class<DeletePriceSetCommand> getCommandType() {
        return DeletePriceSetCommand.class;
    }
}
