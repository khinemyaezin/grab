package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.model.write.UpdateProductStatusCommand;
import com.catalog.application.model.write.UpdateProductStatusResult;
import com.catalog.application.port.inbound.UpdateProductStatusUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateProductStatusCommandHandler implements CommandHandler<UpdateProductStatusCommand, UpdateProductStatusResult> {

    private final UpdateProductStatusUseCase updateProductStatusUseCase;

    @Override
    @CatalogTransactional
    public UpdateProductStatusResult handle(UpdateProductStatusCommand command) {
        return updateProductStatusUseCase.execute(command);
    }

    @Override
    public Class<UpdateProductStatusCommand> getCommandType() {
        return UpdateProductStatusCommand.class;
    }
}
