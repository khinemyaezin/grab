package com.catalog.application.port.inbound;

import com.catalog.application.command.UpdateProductCommand;
import com.catalog.application.command.UpdateProductResult;

public interface UpdateProductUseCase {
    UpdateProductResult execute(UpdateProductCommand command);
}
