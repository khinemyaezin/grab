package com.grab.app.merchandising.catalog.internal.command.handler;

import com.catalog.application.model.write.DeleteProductCommand;
import com.catalog.application.model.write.DeleteProductResult;
import com.catalog.application.port.inbound.DeleteProductUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeleteProductCommandHandler implements CommandHandler<DeleteProductCommand, DeleteProductResult> {

    private final DeleteProductUseCase deleteProductUseCase;

    public DeleteProductCommandHandler(DeleteProductUseCase deleteProductUseCase) {
        this.deleteProductUseCase = deleteProductUseCase;
    }

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
