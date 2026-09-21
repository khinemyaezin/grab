package com.catalog.application.port.inbound;

import com.catalog.application.model.write.ApplyProductStatusCommand;
import com.catalog.application.model.write.ApplyProductStatusResult;

public interface ApplyProductStatusUseCase {
    ApplyProductStatusResult execute(ApplyProductStatusCommand command);
}
