package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.command.UpdateVariantCommand;
import com.catalog.application.command.UpdateVariantResult;
import com.catalog.application.port.inbound.UpdateVariantUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateVariantCommandHandler implements CommandHandler<UpdateVariantCommand, UpdateVariantResult> {

    private final UpdateVariantUseCase updateVariantUseCase;

    @Override
    @CatalogTransactional
    public UpdateVariantResult handle(UpdateVariantCommand command) {
        return updateVariantUseCase.execute(command);
    }

    @Override
    public Class<UpdateVariantCommand> getCommandType() {
        return UpdateVariantCommand.class;
    }
}
