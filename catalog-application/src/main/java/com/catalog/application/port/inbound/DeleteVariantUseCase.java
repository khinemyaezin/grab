package com.catalog.application.port.inbound;

import com.catalog.application.model.write.DeleteVariantCommand;
import com.catalog.application.model.write.DeleteVariantResult;

public interface DeleteVariantUseCase {
    DeleteVariantResult execute(DeleteVariantCommand command);
}
