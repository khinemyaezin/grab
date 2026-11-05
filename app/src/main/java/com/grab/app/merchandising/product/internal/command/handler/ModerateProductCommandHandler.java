package com.grab.app.merchandising.product.internal.command.handler;

import com.catalog.application.model.write.ModerateProductCommand;
import com.catalog.application.model.write.ModerateProductResult;
import com.catalog.application.port.inbound.ModerateProductUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class ModerateProductCommandHandler implements CommandHandler<ModerateProductCommand, ModerateProductResult> {

    private final ModerateProductUseCase moderateProductUseCase;

    public ModerateProductCommandHandler(ModerateProductUseCase moderateProductUseCase) {
        this.moderateProductUseCase = moderateProductUseCase;
    }

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
