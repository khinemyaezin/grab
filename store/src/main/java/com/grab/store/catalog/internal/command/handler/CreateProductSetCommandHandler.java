package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.model.write.CreateProductSetCommand;
import com.catalog.application.model.write.CreateProductSetResult;
import com.catalog.application.port.inbound.CreateProductSetUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateProductSetCommandHandler implements CommandHandler<CreateProductSetCommand, CreateProductSetResult> {

    private final CreateProductSetUseCase createProductSetUseCase;

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
