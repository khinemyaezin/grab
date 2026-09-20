package com.catalog.application.port.inbound;

import com.catalog.application.command.UpsertMerchantAvailabilityCommand;

public interface UpsertMerchantAvailabilityUseCase {
    Void execute(UpsertMerchantAvailabilityCommand command);
}
