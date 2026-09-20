package com.catalog.application.port.inbound;

import com.catalog.application.command.ProductMediaResult;
import com.catalog.application.command.ReplaceProductMediaCommand;

public interface ReplaceProductMediaUseCase {
    ProductMediaResult execute(ReplaceProductMediaCommand command);
}
