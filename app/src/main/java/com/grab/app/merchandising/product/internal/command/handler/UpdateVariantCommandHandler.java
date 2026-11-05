package com.grab.app.merchandising.product.internal.command.handler;

import com.catalog.application.model.write.UpdateVariantCommand;
import com.catalog.application.model.write.UpdateVariantResult;
import com.catalog.application.port.inbound.UpdateVariantUseCase;
import com.grab.app.merchandising.product.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class UpdateVariantCommandHandler implements CommandHandler<UpdateVariantCommand, UpdateVariantResult> {

    private final UpdateVariantUseCase updateVariantUseCase;

    public UpdateVariantCommandHandler(UpdateVariantUseCase updateVariantUseCase) {
        this.updateVariantUseCase = updateVariantUseCase;
    }

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
