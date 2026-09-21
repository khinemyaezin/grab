package com.catalog.application.port.inbound;

import com.catalog.application.model.write.CreateProductSetCommand;
import com.catalog.application.model.write.CreateProductSetResult;

public interface CreateProductSetUseCase {
    CreateProductSetResult execute(CreateProductSetCommand command);
}
