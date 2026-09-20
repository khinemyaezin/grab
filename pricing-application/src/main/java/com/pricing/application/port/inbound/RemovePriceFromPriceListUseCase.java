package com.pricing.application.port.inbound;

import com.pricing.application.model.write.RemovePriceFromPriceListCommand;
import com.pricing.application.model.write.PriceListResult;

public interface RemovePriceFromPriceListUseCase {
    PriceListResult execute(RemovePriceFromPriceListCommand command);
}
