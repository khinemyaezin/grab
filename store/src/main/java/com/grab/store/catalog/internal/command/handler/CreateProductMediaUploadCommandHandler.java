package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.command.CreateProductMediaUploadCommand;
import com.catalog.application.command.ProductMediaUploadResult;
import com.catalog.application.port.inbound.CreateProductMediaUploadUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateProductMediaUploadCommandHandler implements CommandHandler<CreateProductMediaUploadCommand, ProductMediaUploadResult> {

    private final CreateProductMediaUploadUseCase createProductMediaUploadUseCase;

    @Override
    @CatalogReadTransactional
    public ProductMediaUploadResult handle(CreateProductMediaUploadCommand command) {
        return createProductMediaUploadUseCase.execute(command);
    }

    @Override
    public Class<CreateProductMediaUploadCommand> getCommandType() {
        return CreateProductMediaUploadCommand.class;
    }
}
