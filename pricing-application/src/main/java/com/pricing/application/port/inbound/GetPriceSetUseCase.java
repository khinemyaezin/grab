package com.pricing.application.port.inbound;

import com.pricing.application.model.read.GetPriceSetQuery;
import com.pricing.application.model.write.PriceSetResult;

public interface GetPriceSetUseCase {
    PriceSetResult execute(GetPriceSetQuery query);
}
