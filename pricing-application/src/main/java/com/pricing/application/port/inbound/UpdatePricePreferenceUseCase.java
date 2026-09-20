package com.pricing.application.port.inbound;

import com.pricing.application.model.write.UpdatePricePreferenceCommand;
import com.pricing.application.model.write.PricePreferenceResult;

public interface UpdatePricePreferenceUseCase {
    PricePreferenceResult execute(UpdatePricePreferenceCommand command);
}
