package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.command.DeleteProductCommand;
import com.catalog.application.command.DeleteProductResult;
import com.catalog.application.port.inbound.DeleteProductUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteProductCommandHandler implements CommandHandler<DeleteProductCommand, DeleteProductResult> {

    private final DeleteProductUseCase deleteProductUseCase;

    @Override
    @CatalogTransactional
    public DeleteProductResult handle(DeleteProductCommand command) {
        return deleteProductUseCase.execute(command);
    }

    @Override
    public Class<DeleteProductCommand> getCommandType() {
        return DeleteProductCommand.class;
    }
}
