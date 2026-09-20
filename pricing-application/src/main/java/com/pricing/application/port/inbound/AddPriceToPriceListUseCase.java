package com.pricing.application.port.inbound;

import com.pricing.application.model.write.AddPriceToPriceListCommand;
import com.pricing.application.model.write.PriceListResult;

public interface AddPriceToPriceListUseCase {
    PriceListResult execute(AddPriceToPriceListCommand command);
}
