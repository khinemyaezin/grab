package com.catalog.application.port.inbound;

import com.catalog.application.command.CreateProductMediaUploadCommand;
import com.catalog.application.command.ProductMediaUploadResult;

public interface CreateProductMediaUploadUseCase {
    ProductMediaUploadResult execute(CreateProductMediaUploadCommand command);
}
