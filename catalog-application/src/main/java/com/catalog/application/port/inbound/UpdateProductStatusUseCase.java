package com.catalog.application.port.inbound;

import com.catalog.application.command.UpdateProductStatusCommand;
import com.catalog.application.command.UpdateProductStatusResult;

public interface UpdateProductStatusUseCase {
    UpdateProductStatusResult execute(UpdateProductStatusCommand command);
}
