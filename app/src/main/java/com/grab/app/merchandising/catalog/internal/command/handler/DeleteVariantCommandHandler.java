package com.grab.app.merchandising.catalog.internal.command.handler;

import com.catalog.application.model.write.DeleteVariantCommand;
import com.catalog.application.model.write.DeleteVariantResult;
import com.catalog.application.port.inbound.DeleteVariantUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeleteVariantCommandHandler implements CommandHandler<DeleteVariantCommand, DeleteVariantResult> {

    private final DeleteVariantUseCase deleteVariantUseCase;

    public DeleteVariantCommandHandler(DeleteVariantUseCase deleteVariantUseCase) {
        this.deleteVariantUseCase = deleteVariantUseCase;
    }

    @Override
    @CatalogTransactional
    public DeleteVariantResult handle(DeleteVariantCommand command) {
        return deleteVariantUseCase.execute(command);
    }

    @Override
    public Class<DeleteVariantCommand> getCommandType() {
        return DeleteVariantCommand.class;
    }
}
