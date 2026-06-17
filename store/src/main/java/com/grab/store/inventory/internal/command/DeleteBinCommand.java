package com.grab.store.inventory.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record DeleteBinCommand(
        Id binId,
        String actorId
) implements Command<Void> {
}
