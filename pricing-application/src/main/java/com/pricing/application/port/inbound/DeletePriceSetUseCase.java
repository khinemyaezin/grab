package com.pricing.application.port.inbound;

import com.pricing.application.model.write.DeletePriceSetCommand;

public interface DeletePriceSetUseCase {
    Void execute(DeletePriceSetCommand command);
}
