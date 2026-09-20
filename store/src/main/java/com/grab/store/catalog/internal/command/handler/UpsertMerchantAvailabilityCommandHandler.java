package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.model.write.UpsertMerchantAvailabilityCommand;
import com.catalog.application.port.inbound.UpsertMerchantAvailabilityUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpsertMerchantAvailabilityCommandHandler implements CommandHandler<UpsertMerchantAvailabilityCommand, Void> {

    private final UpsertMerchantAvailabilityUseCase upsertMerchantAvailabilityUseCase;

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
