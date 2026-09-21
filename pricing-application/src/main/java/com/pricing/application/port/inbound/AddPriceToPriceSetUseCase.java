package com.pricing.application.port.inbound;

import com.pricing.application.model.write.AddPriceToPriceSetCommand;
import com.pricing.application.model.write.PriceSetResult;

public interface AddPriceToPriceSetUseCase {
    PriceSetResult execute(AddPriceToPriceSetCommand command);
}
