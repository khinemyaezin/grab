package com.catalog.application.port.inbound;

import com.catalog.application.model.write.UpdateProductCommand;
import com.catalog.application.model.write.UpdateProductResult;

public interface UpdateProductUseCase {
    UpdateProductResult execute(UpdateProductCommand command);
}
