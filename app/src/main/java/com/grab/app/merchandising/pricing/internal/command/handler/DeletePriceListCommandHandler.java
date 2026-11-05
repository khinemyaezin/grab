package com.grab.app.merchandising.pricing.internal.command.handler;

import com.grab.app.merchandising.pricing.internal.config.PricingTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import com.pricing.application.model.write.DeletePriceListCommand;
import com.pricing.application.port.inbound.DeletePriceListUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeletePriceListCommandHandler implements CommandHandler<DeletePriceListCommand, Void> {

    private final DeletePriceListUseCase deletePriceListUseCase;

    public DeletePriceListCommandHandler(DeletePriceListUseCase deletePriceListUseCase) {
        this.deletePriceListUseCase = deletePriceListUseCase;
    }

    @Override
    @PricingTransactional
    public Void handle(DeletePriceListCommand command) {
        return deletePriceListUseCase.execute(command);
    }

    @Override
    public Class<DeletePriceListCommand> getCommandType() {
        return DeletePriceListCommand.class;
    }
}

