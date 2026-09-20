package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.command.UpdateProductCommand;
import com.catalog.application.command.UpdateProductResult;
import com.catalog.application.port.inbound.UpdateProductUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateProductCommandHandler implements CommandHandler<UpdateProductCommand, UpdateProductResult> {

    private final UpdateProductUseCase updateProductUseCase;

    @Override
    @CatalogTransactional
    public UpdateProductResult handle(UpdateProductCommand command) {
        return updateProductUseCase.execute(command);
    }

    @Override
    public Class<UpdateProductCommand> getCommandType() {
        return UpdateProductCommand.class;
    }
}
