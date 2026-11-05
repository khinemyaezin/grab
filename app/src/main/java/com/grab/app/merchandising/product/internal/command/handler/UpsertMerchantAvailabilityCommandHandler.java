package com.grab.app.merchandising.product.internal.command.handler;

import com.catalog.application.model.write.UpsertMerchantAvailabilityCommand;

import com.catalog.application.port.inbound.UpsertMerchantAvailabilityUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UpsertMerchantAvailabilityCommandHandler implements CommandHandler<UpsertMerchantAvailabilityCommand, Void> {

    private final UpsertMerchantAvailabilityUseCase upsertMerchantAvailabilityUseCase;

    public UpsertMerchantAvailabilityCommandHandler(UpsertMerchantAvailabilityUseCase upsertMerchantAvailabilityUseCase) {
        this.upsertMerchantAvailabilityUseCase = upsertMerchantAvailabilityUseCase;
    }

    @Override
    @CatalogTransactional
    public Void handle(UpsertMerchantAvailabilityCommand command) {
        return upsertMerchantAvailabilityUseCase.execute(command);
    }

    @Override
    public Class<UpsertMerchantAvailabilityCommand> getCommandType() {
        return UpsertMerchantAvailabilityCommand.class;
    }
}
