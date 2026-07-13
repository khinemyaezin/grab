package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record ActivateBinCommand(
        Id binId,
        String actorId,
        String scopeKey,
        String scopeId
) implements Command<BinResult> {
}
