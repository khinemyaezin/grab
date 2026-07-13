package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record ReleaseReservationCommand(
        Id inventoryItemId,
        Id reservationId,
        Id createdBy
,
        String scopeKey,
        String scopeId
) implements Command<InventoryReservationResult> {
}
