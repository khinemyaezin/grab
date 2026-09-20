package com.catalog.application.port.inbound;

import com.catalog.application.command.ModerateProductCommand;
import com.catalog.application.command.ModerateProductResult;

public interface ModerateProductUseCase {
    ModerateProductResult execute(ModerateProductCommand command);
}
