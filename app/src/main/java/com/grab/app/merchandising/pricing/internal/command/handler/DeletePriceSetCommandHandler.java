package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.DeletePriceSetCommand;
import com.pricing.application.port.inbound.DeletePriceSetUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeletePriceSetCommandHandler implements CommandHandler<DeletePriceSetCommand, Void> {

    private final DeletePriceSetUseCase deletePriceSetUseCase;

    public DeletePriceSetCommandHandler(DeletePriceSetUseCase deletePriceSetUseCase) {
        this.deletePriceSetUseCase = deletePriceSetUseCase;
    }

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

