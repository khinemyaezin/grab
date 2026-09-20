package com.catalog.application.port.inbound;

import com.catalog.application.model.write.UpsertMerchantAvailabilityCommand;

public interface UpsertMerchantAvailabilityUseCase {
    Void execute(UpsertMerchantAvailabilityCommand command);
}
