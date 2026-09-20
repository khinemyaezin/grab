package com.catalog.application.port.inbound;

import com.catalog.application.model.write.UpdateProductStatusCommand;
import com.catalog.application.model.write.UpdateProductStatusResult;

public interface UpdateProductStatusUseCase {
    UpdateProductStatusResult execute(UpdateProductStatusCommand command);
}
