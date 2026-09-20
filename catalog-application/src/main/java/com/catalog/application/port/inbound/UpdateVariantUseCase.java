package com.catalog.application.port.inbound;

import com.catalog.application.model.write.UpdateVariantCommand;
import com.catalog.application.model.write.UpdateVariantResult;

public interface UpdateVariantUseCase {
    UpdateVariantResult execute(UpdateVariantCommand command);
}
