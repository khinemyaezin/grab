package com.catalog.application.port.inbound;

import com.catalog.application.command.ProductDescriptionsResult;
import com.catalog.application.command.ReplaceProductDescriptionsCommand;

public interface ReplaceProductDescriptionsUseCase {
    ProductDescriptionsResult execute(ReplaceProductDescriptionsCommand command);
}
