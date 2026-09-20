package com.catalog.application.port.inbound;

import com.catalog.application.model.write.DeleteCategoryCommand;
import com.catalog.application.model.write.DeleteCategoryResult;

public interface DeleteCategoryUseCase {
    DeleteCategoryResult execute(DeleteCategoryCommand command);
}
