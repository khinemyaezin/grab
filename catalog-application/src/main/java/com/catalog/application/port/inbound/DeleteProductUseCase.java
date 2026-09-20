package com.catalog.application.port.inbound;

import com.catalog.application.command.DeleteProductCommand;
import com.catalog.application.command.DeleteProductResult;

public interface DeleteProductUseCase {
    DeleteProductResult execute(DeleteProductCommand command);
}
