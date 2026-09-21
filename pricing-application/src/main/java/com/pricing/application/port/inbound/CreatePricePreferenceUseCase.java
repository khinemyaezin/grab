package com.pricing.application.port.inbound;

import com.pricing.application.model.write.CreatePricePreferenceCommand;
import com.pricing.application.model.write.PricePreferenceResult;

public interface CreatePricePreferenceUseCase {
    PricePreferenceResult execute(CreatePricePreferenceCommand command);
}
