package com.catalog.application.port.inbound;

import com.catalog.application.command.DeleteVariantCommand;
import com.catalog.application.command.DeleteVariantResult;

public interface DeleteVariantUseCase {
    DeleteVariantResult execute(DeleteVariantCommand command);
}
