package com.pricing.application.port.inbound;

import com.pricing.application.model.write.DeletePricePreferenceCommand;

public interface DeletePricePreferenceUseCase {
    Void execute(DeletePricePreferenceCommand command);
}
