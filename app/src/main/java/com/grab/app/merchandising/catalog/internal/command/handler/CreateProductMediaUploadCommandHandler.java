package com.grab.app.merchandising.catalog.internal.command.handler;

import com.catalog.application.model.write.CreateProductMediaUploadCommand;
import com.catalog.application.model.write.ProductMediaUploadResult;
import com.catalog.application.port.inbound.CreateProductMediaUploadUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateProductMediaUploadCommandHandler implements CommandHandler<CreateProductMediaUploadCommand, ProductMediaUploadResult> {

    private final CreateProductMediaUploadUseCase createProductMediaUploadUseCase;

    public CreateProductMediaUploadCommandHandler(CreateProductMediaUploadUseCase createProductMediaUploadUseCase) {
        this.createProductMediaUploadUseCase = createProductMediaUploadUseCase;
    }

    @Override
    @CatalogTransactional
    public ProductMediaUploadResult handle(CreateProductMediaUploadCommand command) {
        return createProductMediaUploadUseCase.execute(command);
    }

    @Override
    public Class<CreateProductMediaUploadCommand> getCommandType() {
        return CreateProductMediaUploadCommand.class;
    }
}
