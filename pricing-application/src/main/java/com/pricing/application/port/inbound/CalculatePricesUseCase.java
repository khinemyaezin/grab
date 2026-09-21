package com.pricing.application.port.inbound;

import com.pricing.application.model.read.CalculatePricesQuery;
import java.util.List;
import com.pricing.application.model.read.CalculatedPriceSetResult;

public interface CalculatePricesUseCase {
    List<CalculatedPriceSetResult> execute(CalculatePricesQuery query);
}
