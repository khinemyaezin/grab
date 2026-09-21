package com.pricing.application.port.inbound;

import com.pricing.application.model.write.CreatePriceSetCommand;
import com.pricing.application.model.write.CreatePriceSetResult;

public interface CreatePriceSetUseCase {
    CreatePriceSetResult execute(CreatePriceSetCommand command);
}
