package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.model.write.DeleteVariantCommand;
import com.catalog.application.model.write.DeleteVariantResult;
import com.catalog.application.port.inbound.DeleteVariantUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteVariantCommandHandler implements CommandHandler<DeleteVariantCommand, DeleteVariantResult> {

    private final DeleteVariantUseCase deleteVariantUseCase;

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
