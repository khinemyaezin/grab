package com.catalog.application.port.inbound;

import com.catalog.application.command.CreateProductSetCommand;
import com.catalog.application.command.CreateProductSetResult;

public interface CreateProductSetUseCase {
    CreateProductSetResult execute(CreateProductSetCommand command);
}
