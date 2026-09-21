package com.grab.store.pricing.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.pricing.internal.config.PricingTransactional;
import com.pricing.application.model.write.DeletePriceListCommand;
import com.pricing.application.port.inbound.DeletePriceListUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeletePriceListCommandHandler implements CommandHandler<DeletePriceListCommand, Void> {

    private final DeletePriceListUseCase deletePriceListUseCase;

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
