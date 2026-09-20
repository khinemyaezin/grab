package com.catalog.application.port.inbound;

import com.catalog.application.command.DeleteCategoryCommand;
import com.catalog.application.command.DeleteCategoryResult;

public interface DeleteCategoryUseCase {
    DeleteCategoryResult execute(DeleteCategoryCommand command);
}
