package com.pricing.application.port.inbound;

import com.pricing.application.model.write.UpdatePriceListCommand;
import com.pricing.application.model.write.PriceListResult;

public interface UpdatePriceListUseCase {
    PriceListResult execute(UpdatePriceListCommand command);
}
