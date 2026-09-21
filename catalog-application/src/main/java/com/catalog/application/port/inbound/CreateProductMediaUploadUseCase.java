package com.catalog.application.port.inbound;

import com.catalog.application.model.write.CreateProductMediaUploadCommand;
import com.catalog.application.model.write.ProductMediaUploadResult;

public interface CreateProductMediaUploadUseCase {
    ProductMediaUploadResult execute(CreateProductMediaUploadCommand command);
}
