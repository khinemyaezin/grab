package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.model.write.CreateStagedMediaUploadCommand;
import com.catalog.application.model.write.ProductMediaUploadResult;
import com.catalog.application.port.inbound.CreateStagedMediaUploadUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateStagedMediaUploadCommandHandler implements CommandHandler<CreateStagedMediaUploadCommand, ProductMediaUploadResult> {

    private final CreateStagedMediaUploadUseCase createStagedMediaUploadUseCase;

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
