package com.pricing.application.port.inbound;

import com.pricing.application.model.write.RemovePriceFromPriceSetCommand;
import com.pricing.application.model.write.PriceSetResult;

public interface RemovePriceFromPriceSetUseCase {
    PriceSetResult execute(RemovePriceFromPriceSetCommand command);
}
