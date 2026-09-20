package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.command.DeleteCategoryCommand;
import com.catalog.application.command.DeleteCategoryResult;
import com.catalog.application.port.inbound.DeleteCategoryUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteCategoryCommandHandler implements CommandHandler<DeleteCategoryCommand, DeleteCategoryResult> {

    private final DeleteCategoryUseCase deleteCategoryUseCase;

    @Override
    @CatalogTransactional
    public DeleteCategoryResult handle(DeleteCategoryCommand command) {
        return deleteCategoryUseCase.execute(command);
    }

    @Override
    public Class<DeleteCategoryCommand> getCommandType() {
        return DeleteCategoryCommand.class;
    }
}
