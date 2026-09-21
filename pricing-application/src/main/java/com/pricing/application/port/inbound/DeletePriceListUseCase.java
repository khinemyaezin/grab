package com.pricing.application.port.inbound;

import com.pricing.application.model.write.DeletePriceListCommand;

public interface DeletePriceListUseCase {
    Void execute(DeletePriceListCommand command);
}
