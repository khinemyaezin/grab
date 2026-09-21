package com.pricing.application.port.inbound;

import com.pricing.application.model.write.CreatePriceListCommand;
import com.pricing.application.model.write.PriceListResult;

public interface CreatePriceListUseCase {
    PriceListResult execute(CreatePriceListCommand command);
}
