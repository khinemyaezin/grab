package com.grab.app.merchandising.catalog.internal.command.handler;

import com.catalog.application.model.write.CreateProductSetCommand;
import com.catalog.application.model.write.CreateProductSetResult;
import com.catalog.application.port.inbound.CreateProductSetUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateProductSetCommandHandler implements CommandHandler<CreateProductSetCommand, CreateProductSetResult> {

    private final CreateProductSetUseCase createProductSetUseCase;

    public CreateProductSetCommandHandler(CreateProductSetUseCase createProductSetUseCase) {
        this.createProductSetUseCase = createProductSetUseCase;
    }

    @Override
    @CatalogTransactional
    public CreateProductSetResult handle(CreateProductSetCommand command) {
        return createProductSetUseCase.execute(command);
    }

    @Override
    public Class<CreateProductSetCommand> getCommandType() {
        return CreateProductSetCommand.class;
    }
}
