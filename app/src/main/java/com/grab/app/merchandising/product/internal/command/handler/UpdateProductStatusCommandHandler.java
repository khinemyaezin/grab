package com.grab.app.merchandising.product.internal.command.handler;

import com.catalog.application.model.write.UpdateProductStatusCommand;
import com.catalog.application.model.write.UpdateProductStatusResult;
import com.catalog.application.port.inbound.UpdateProductStatusUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UpdateProductStatusCommandHandler implements CommandHandler<UpdateProductStatusCommand, UpdateProductStatusResult> {

    private final UpdateProductStatusUseCase updateProductStatusUseCase;

    public UpdateProductStatusCommandHandler(UpdateProductStatusUseCase updateProductStatusUseCase) {
        this.updateProductStatusUseCase = updateProductStatusUseCase;
    }

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
