package com.pricing.application.port.inbound;

import com.pricing.application.model.read.GetPriceListQuery;
import com.pricing.application.model.write.PriceListResult;

public interface GetPriceListUseCase {
    PriceListResult execute(GetPriceListQuery query);
}
