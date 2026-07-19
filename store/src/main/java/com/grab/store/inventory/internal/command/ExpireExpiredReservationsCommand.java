package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.time.LocalDateTime;

public record ExpireExpiredReservationsCommand(
        LocalDateTime asOf,
        int batchSize,
        Id createdBy
) implements Command<ExpireExpiredReservationsResult> {
}
