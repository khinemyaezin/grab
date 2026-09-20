package com.catalog.application.port.inbound;

import com.catalog.application.command.UpdateVariantCommand;
import com.catalog.application.command.UpdateVariantResult;

public interface UpdateVariantUseCase {
    UpdateVariantResult execute(UpdateVariantCommand command);
}
