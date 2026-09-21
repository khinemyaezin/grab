package com.pricing.application.port.inbound;

import com.pricing.application.model.write.UpdateVariantPriceCommand;
import com.pricing.application.model.write.UpdateVariantPriceResult;

public interface UpdateVariantPriceUseCase {
    UpdateVariantPriceResult execute(UpdateVariantPriceCommand command);
}
