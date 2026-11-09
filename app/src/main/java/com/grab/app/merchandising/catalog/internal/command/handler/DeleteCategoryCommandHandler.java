package com.grab.app.merchandising.catalog.internal.command.handler;

import com.catalog.application.model.write.DeleteCategoryCommand;
import com.catalog.application.model.write.DeleteCategoryResult;
import com.catalog.application.port.inbound.DeleteCategoryUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class DeleteCategoryCommandHandler implements CommandHandler<DeleteCategoryCommand, DeleteCategoryResult> {

    private final DeleteCategoryUseCase deleteCategoryUseCase;

    public DeleteCategoryCommandHandler(DeleteCategoryUseCase deleteCategoryUseCase) {
        this.deleteCategoryUseCase = deleteCategoryUseCase;
    }

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
