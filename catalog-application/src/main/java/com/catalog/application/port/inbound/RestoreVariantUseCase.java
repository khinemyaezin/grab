package com.catalog.application.port.inbound;

import com.catalog.application.command.RestoreVariantCommand;
import com.catalog.application.command.RestoreVariantResult;

public interface RestoreVariantUseCase {
    RestoreVariantResult execute(RestoreVariantCommand command);
}
