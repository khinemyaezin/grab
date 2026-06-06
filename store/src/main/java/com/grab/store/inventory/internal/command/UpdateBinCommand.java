package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record UpdateBinCommand(
        Id binId,
        String code,
        String name,
        Integer maxCapacity,
        Boolean active,
        String actorId
) implements Command<BinResult> {
}
