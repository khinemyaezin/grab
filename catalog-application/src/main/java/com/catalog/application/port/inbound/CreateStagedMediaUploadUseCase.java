package com.catalog.application.port.inbound;

import com.catalog.application.model.write.CreateStagedMediaUploadCommand;
import com.catalog.application.model.write.ProductMediaUploadResult;

public interface CreateStagedMediaUploadUseCase {
    ProductMediaUploadResult execute(CreateStagedMediaUploadCommand command);
}
