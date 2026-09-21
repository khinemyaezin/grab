package com.catalog.application.port.inbound;

import com.catalog.application.model.write.RestoreVariantCommand;
import com.catalog.application.model.write.RestoreVariantResult;

public interface RestoreVariantUseCase {
    RestoreVariantResult execute(RestoreVariantCommand command);
}
