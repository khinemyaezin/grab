package com.grab.app.merchandising.catalog.internal.command.handler;

import com.catalog.application.model.write.UpdateProductCommand;
import com.catalog.application.model.write.UpdateProductResult;
import com.catalog.application.port.inbound.UpdateProductUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UpdateProductCommandHandler implements CommandHandler<UpdateProductCommand, UpdateProductResult> {

    private final UpdateProductUseCase updateProductUseCase;

    public UpdateProductCommandHandler(UpdateProductUseCase updateProductUseCase) {
        this.updateProductUseCase = updateProductUseCase;
    }

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
