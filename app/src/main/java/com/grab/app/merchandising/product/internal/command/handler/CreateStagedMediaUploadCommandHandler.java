package com.grab.app.merchandising.product.internal.command.handler;

import com.catalog.application.model.write.CreateStagedMediaUploadCommand;
import com.catalog.application.model.write.ProductMediaUploadResult;
import com.catalog.application.port.inbound.CreateStagedMediaUploadUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateStagedMediaUploadCommandHandler implements CommandHandler<CreateStagedMediaUploadCommand, ProductMediaUploadResult> {

    private final CreateStagedMediaUploadUseCase createStagedMediaUploadUseCase;

    public CreateStagedMediaUploadCommandHandler(CreateStagedMediaUploadUseCase createStagedMediaUploadUseCase) {
        this.createStagedMediaUploadUseCase = createStagedMediaUploadUseCase;
    }

    @Override
    @CatalogTransactional
    public ProductMediaUploadResult handle(CreateStagedMediaUploadCommand command) {
        return createStagedMediaUploadUseCase.execute(command);
    }

    @Override
    public Class<CreateStagedMediaUploadCommand> getCommandType() {
        return CreateStagedMediaUploadCommand.class;
    }
}
