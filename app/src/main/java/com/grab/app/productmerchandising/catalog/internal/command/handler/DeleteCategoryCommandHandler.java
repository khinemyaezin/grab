package com.grab.app.productmerchandising.catalog.internal.command.handler;

import com.catalog.application.model.write.DeleteCategoryCommand;
import com.catalog.application.model.write.DeleteCategoryResult;
import com.catalog.application.port.inbound.DeleteCategoryUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.app.productmerchandising.catalog.internal.config.CatalogTransactional;
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
