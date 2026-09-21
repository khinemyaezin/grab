package com.catalog.application.port.inbound;

import com.catalog.application.model.write.ProductDescriptionsResult;
import com.catalog.application.model.write.ReplaceProductDescriptionsCommand;

public interface ReplaceProductDescriptionsUseCase {
    ProductDescriptionsResult execute(ReplaceProductDescriptionsCommand command);
}
