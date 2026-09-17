package com.grab.store.catalog.internal.command;

import com.grab.framework.cqrs.command.Command;

public record UpsertMerchantAvailabilityCommand(
        String merchantId,
        String status,
        String merchantType
) implements Command<Void> {
}
