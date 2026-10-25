package com.grab.app.productmerchandising.catalog.internal.command.handler;

import com.catalog.application.model.write.UpdateProductCommand;
import com.catalog.application.model.write.UpdateProductResult;
import com.catalog.application.port.inbound.UpdateProductUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.app.productmerchandising.catalog.internal.config.CatalogTransactional;
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
