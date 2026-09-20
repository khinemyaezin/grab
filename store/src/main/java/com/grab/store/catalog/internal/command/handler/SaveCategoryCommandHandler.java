package com.grab.store.catalog.internal.command.handler;

import com.catalog.application.model.write.SaveCategoryCommand;
import com.catalog.application.model.write.SaveCategoryResult;
import com.catalog.application.port.inbound.SaveCategoryUseCase;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveCategoryCommandHandler implements CommandHandler<SaveCategoryCommand, SaveCategoryResult> {

    private final SaveCategoryUseCase saveCategoryUseCase;

    @Override
    @CatalogTransactional
    public SaveCategoryResult handle(SaveCategoryCommand command) {
        return saveCategoryUseCase.execute(command);
    }

    @Override
    public Class<SaveCategoryCommand> getCommandType() {
        return SaveCategoryCommand.class;
    }
}
