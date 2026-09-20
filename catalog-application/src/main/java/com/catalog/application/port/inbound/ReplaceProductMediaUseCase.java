package com.catalog.application.port.inbound;

import com.catalog.application.model.write.ProductMediaResult;
import com.catalog.application.model.write.ReplaceProductMediaCommand;

public interface ReplaceProductMediaUseCase {
    ProductMediaResult execute(ReplaceProductMediaCommand command);
}
