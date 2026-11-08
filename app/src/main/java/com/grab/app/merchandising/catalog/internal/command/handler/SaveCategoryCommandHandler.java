package com.grab.app.merchandising.catalog.internal.command.handler;

import com.catalog.application.model.write.SaveCategoryCommand;
import com.catalog.application.model.write.SaveCategoryResult;
import com.catalog.application.port.inbound.SaveCategoryUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogTransactional;
import com.grab.framework.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class SaveCategoryCommandHandler implements CommandHandler<SaveCategoryCommand, SaveCategoryResult> {

    private final SaveCategoryUseCase saveCategoryUseCase;

    public SaveCategoryCommandHandler(SaveCategoryUseCase saveCategoryUseCase) {
        this.saveCategoryUseCase = saveCategoryUseCase;
    }

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
