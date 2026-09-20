package com.catalog.application.port.inbound;

import com.catalog.application.command.SaveCategoryCommand;
import com.catalog.application.command.SaveCategoryResult;

public interface SaveCategoryUseCase {
    SaveCategoryResult execute(SaveCategoryCommand command);
}
