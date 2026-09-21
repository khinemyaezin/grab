package com.catalog.application.port.inbound;

import com.catalog.application.model.write.ModerateProductCommand;
import com.catalog.application.model.write.ModerateProductResult;

public interface ModerateProductUseCase {
    ModerateProductResult execute(ModerateProductCommand command);
}
