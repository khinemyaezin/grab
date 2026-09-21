package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.model.write.ModerateProductCommand;
import com.catalog.application.model.write.ModerateProductResult;
import com.catalog.application.port.inbound.ModerateProductUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModerateProductCommandHandler implements CommandHandler<ModerateProductCommand, ModerateProductResult> {

    private final ModerateProductUseCase moderateProductUseCase;

    @Override
    @CatalogTransactional
    public ModerateProductResult handle(ModerateProductCommand command) {
        return moderateProductUseCase.execute(command);
    }

    @Override
    public Class<ModerateProductCommand> getCommandType() {
        return ModerateProductCommand.class;
    }
}
