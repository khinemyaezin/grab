package com.pricing.application.port.inbound;

import com.pricing.application.model.write.UpdatePriceOnPriceSetCommand;
import com.pricing.application.model.write.PriceSetResult;

public interface UpdatePriceOnPriceSetUseCase {
    PriceSetResult execute(UpdatePriceOnPriceSetCommand command);
}
