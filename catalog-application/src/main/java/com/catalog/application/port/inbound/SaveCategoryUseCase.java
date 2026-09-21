package com.catalog.application.port.inbound;

import com.catalog.application.model.write.SaveCategoryCommand;
import com.catalog.application.model.write.SaveCategoryResult;

public interface SaveCategoryUseCase {
    SaveCategoryResult execute(SaveCategoryCommand command);
}
