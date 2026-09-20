package com.catalog.application.port.inbound;

import com.catalog.application.model.write.DeleteProductCommand;
import com.catalog.application.model.write.DeleteProductResult;

public interface DeleteProductUseCase {
    DeleteProductResult execute(DeleteProductCommand command);
}
