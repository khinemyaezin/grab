package com.pricing.application.port.inbound;

import com.pricing.application.model.read.GetPricePreferenceQuery;
import com.pricing.application.model.write.PricePreferenceResult;

public interface GetPricePreferenceUseCase {
    PricePreferenceResult execute(GetPricePreferenceQuery query);
}
