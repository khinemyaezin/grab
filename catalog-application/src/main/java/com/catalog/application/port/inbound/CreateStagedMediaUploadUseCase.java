package com.catalog.application.port.inbound;

import com.catalog.application.command.CreateStagedMediaUploadCommand;
import com.catalog.application.command.ProductMediaUploadResult;

public interface CreateStagedMediaUploadUseCase {
    ProductMediaUploadResult execute(CreateStagedMediaUploadCommand command);
}
