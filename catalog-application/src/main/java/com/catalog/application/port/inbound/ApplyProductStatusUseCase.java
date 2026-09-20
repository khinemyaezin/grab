package com.catalog.application.port.inbound;

import com.catalog.application.command.ApplyProductStatusCommand;
import com.catalog.application.command.ApplyProductStatusResult;

public interface ApplyProductStatusUseCase {
    ApplyProductStatusResult execute(ApplyProductStatusCommand command);
}
